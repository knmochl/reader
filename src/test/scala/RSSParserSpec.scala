package com.allandria.knmochl.reader

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import org.scalatest.matchers._
import org.scalatest._

import akka.actor._
import akka.util.Timeout
import akka.pattern.ask

class RSSParserSpec extendsFlatSpec with ShouldMatchers {
  behavior of "RSSParser"

  implicit val timeout = Timeout(5 seconds)
  implicit val ec = ExecutionContext.global
  val system = ActorSystem("TestReader")

  it should "parse an RSS 0.92 feed" in {
    val parser = system.actorOf(Props[RSSParser])
    val f = parser ? ParseRSS(rssFeedString)
    f onSuccess {
      case RSSParsed(feed) =>
        feed.getTitle() should be("The Title")
        feed.getEntries().length() should be(3)
      case _ =>
        throw new Exception("Wrong reply message from parser")
    }
    system.stop(parser)
  }

}
