package com.allandria.knmochl.reader

import akka.actor._
import scala.concurrent.{Future, future, ExecutionContext}
import ExecutionContext.Implicits.global
import com.sun.syndication.io._
import com.sun.syndication.feed.synd._
import java.io.StringReader

sealed trait RSSParserIncoming
case class ParseRSS(body: String) extends RSSParserIncoming

sealed trait RSSParserOutgoing
case class RSSParsed(feed: SyndFeed) extends RSSParserOutgoing

class RSSParser extends Actor {

  override def receive = {
    case incoming: RSSParserIncoming => {
      val f = incoming match {
        case ParseRSS(body) =>
          RSSParser.parseRSS(body)
      }

      val currentSender = sender
      f onSuccess { case parsed: RSSParsed => currentSender ! parsed }
      f onFailure { case t: Throwable => currentSender ! t }
    }
  }

}

object RSSParser {

  def parseRSS(body: String): Future[RSSParsed] = {
    future {
      val feed = new SyndFeedInput().build(new StringReader(body))
      RSSParsed(feed)
    }
  }

}
