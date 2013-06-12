package com.allandria.knmochl.reader

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Await

import org.scalatest.matchers._
import org.scalatest._

import akka.actor._
import akka.util.Timeout
import akka.pattern.ask

import javax.servlet.http.HttpServletResponse

class URLFetcherSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  behavior of "URLFetcher"

  implicit val timeout = Timeout(5 seconds)
  implicit val ec = ExecutionContext.global
  var httpServer: TestHttpServer = null
  val system = ActorSystem("TestReader")

  override def beforeAll = {
    httpServer = new TestHttpServer()
    httpServer.start()
  }

  override def afterAll = {
    httpServer.stop()
    httpServer = null
  }

  it should "fetch an url" in {
    val fetcher = system.actorOf(Props[URLFetcher])
    val f = fetcher ? FetchURL(httpServer.resolve("/hello"))
    f onSuccess {
      case URLFetched(status, headers, body) =>
        status should be(HttpServletResponse.SC_OK)
        body should be("Hello\n")
      case _ =>
        throw new Exception("Wrong reply message from fetcher")
    }
    system.stop(fetcher)
  }

  it should "handle a 404" in {
    val fetcher = system.actorOf(Props[URLFetcher])
    val f = fetcher ? FetchURL(httpServer.resolve("/nothere"))
    f onSuccess {
      case URLFetched(status, headers, body) =>
        status should be(HttpServletResponse.SC_NOT_FOUND)
      case _ =>
        throw new Exception("Wrong reply message from fetcher")
    }
    system.stop(fetcher)
  }

  it should "fetch a url with a parameter" in {
    val fetcher = system.actorOf(Props[URLFetcher])
    val f = fetcher ? FetchURL(httpServer.resolve("/echo", "what", "this"))
    f onSuccess {
      case URLFetched(status, headers, body) =>
        status should be(HttpServletResponse.SC_OK)
        body should be("this")
      case _ =>
        throw new Exception("Wrong reply message from fetcher")
    }
    system.stop(fetcher)
  }

  it should "fetch many urls in parallel" in {
    // the httpServer only has a fixed number of threads so if you make latency
    // or number of requests too high, the futures will start to time out
    httpServer.withRandomLatency(300) {
      val fetcher = system.actorOf(Props[URLFetcher])
      val numToFetch = 500
      val responses = for (i <- 1 to numToFetch)
        yield (fetcher ? FetchURL(httpServer.resolve("/echo", "what", i.toString)), i)

      val completionOrder = new java.util.concurrent.ConcurrentLinkedQueue[Int]()

      responses.foreach { tuple =>
        tuple._1.onComplete({ f =>
          completionOrder.add(tuple._2)
        })
      }

      var nFetched = 0
      responses foreach { tuple =>
        val f = tuple._1
        val expected = tuple._2.toString
        Await.result(f, 5 seconds) match {
          case URLFetched(status, headers, body) =>
            status should be(HttpServletResponse.SC_OK)
            body should be(expected)
            nFetched += 1
          case t: Throwable =>
            throw t
          case _ =>
            throw new Exception("Wrong reply message from fetcher")
        }
      }
      nFetched should be(numToFetch)

      val completed = completionOrder.asScala.toList
      completed.length should be(numToFetch)
      // the random latency should mean we completed in semi-random roder
      completed should not be(completed.sorted)

      system.stop(fetcher)
    }
  }
}
