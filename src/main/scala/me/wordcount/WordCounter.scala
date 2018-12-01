package me.wordcount

import akka.NotUsed
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Try}

object WordCounter {

  type CountResult = Map[String, Int]

  def count(first: CharacterReader, rest: CharacterReader*)
           (implicit mat: ActorMaterializer): Future[CountResult] = count(first :: rest.toList)

  def count(readers: List[CharacterReader])(implicit mat: ActorMaterializer): Future[CountResult] =
    Source(readers)
      .async("akka.actor.default-blocking-io-dispatcher")
      .flatMapMerge(readers.length, WordSource.build)
      .async("akka.actor.default-dispatcher")
      .scan(Map.empty[String, Int]) { (result, message) =>
        result + (message -> (result.getOrElse(message, 0) + 1))
      }
      .wireTap(
        Flow[CountResult]
          .buffer(1, OverflowStrategy.dropHead)
          .throttle(1, 5 seconds)
          .map(println)
          .to(Sink.ignore)
      )
      .runWith(Sink.last)

}

//todo: other word separators .:, etc.
object WordSource {
  def build(cr: CharacterReader): Source[String, NotUsed] = {

    // todo: rewrite as a stream stage?
    def nextWord: Option[String] = {
      @tailrec
      def loop(word: String): Option[String] =
        Try(cr.nextCharacter()) match {
          case Success(' ') if word.nonEmpty => Some(word)
          case Success(' ') => loop("")
          case Success(c) => loop(word + c)
          case _ if word.nonEmpty => Some(word)
          case _ => None
        }

      loop("")
    }

    Source
      .repeat(NotUsed)
      .map(_ => nextWord)
      .takeWhile(_.nonEmpty, inclusive = true)
      .flatMapConcat {
        case Some(word) => Source.single(word)
        case None =>
          cr.close()
          Source.empty
      }

  }

}
