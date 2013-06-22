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

  val rssFeedString =
    """|<?xml version="1.0"?>
       |<rss version="0.92">
       |  <channel>
       |    <title>Test Feed</title>
       |    <link>http://localhost/</link>
       |    <description>A test feed</description>
       |    <item>
       |      <description>Feed entry 1</description>
       |    </item>
       |    <item>
       |      <description>Feed entry 2</description>
       |    </item>
       |    <item>
       |      <description>Feed entry 3</description>
       |    </item>
       |  </channel>
       |</rss>""".stripMargin

  val atomFeedString =
    """|<?xml version="1.0" encoding="utf-8"?>
       |<feed xmlns="http://www.w3.org/2005/Atom">
       |  <title>Test Feed</title>
       |  <entry>
       |    <title>Feed entry 1</title>
       |    <id>foo1</id>
       |    <summary>Text</summary>
       |  </entry>
       |  <entry>
       |    <title>Feed entry 2</title>
       |    <id>foo2</id>
       |    <summary>Text</summary>
       |  </entry>
       |  <entry>
       |    <title>Feed entry 3</title>
       |    <id>foo3</id>
       |    <summary>Text</summary>
       |  </entry>
       |</feed>""".stripMargin

  it should "parse an RSS 0.92 feed" in {
    val parser = system.actorOf(Props[RSSParser])
    val f = parser ? ParseRSS(rssFeedString)
    f onSuccess {
      case RSSParsed(feed) =>
        feed.getTitle() should be("Test Feed")
        feed.getEntries().size should be(3)
      case _ =>
        throw new Exception("Wrong reply message from parser")
    }
    system.stop(parser)
  }

  it should "parse an Atom feed" in {
    val parser = system.actorOf(Props[RSSParser])
    val f = parser ? ParseRSS(atomFeedString)
    f onSuccess {
      case RSSParsed(feed) =>
        feed.getTitle() should be("Test Feed")
        feed.getEntries().size should be(3)
      case _ =>
        throw new Exception("Wrong reply message from parser")
    }
    system.stop(parser)
  }

  it should "receive an exception for a bad format" (pending)

}
