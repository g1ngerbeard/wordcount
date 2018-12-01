package me.wordcount

import akka.NotUsed
import akka.stream.ThrottleMode.Shaping
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Try}

object WordCounter {

  type CountResult = Map[String, Int]

  def count(first: CharacterReader, rest: CharacterReader*)
           (implicit mat: ActorMaterializer): Future[CountResult] = count(first :: rest.toList)

  //todo: async boundaries
  def count(readers: List[CharacterReader])(implicit mat: ActorMaterializer): Future[CountResult] =
    Source(readers)
      .flatMapMerge(readers.length, WordSource.build(_).async)
      .scan(Map.empty[String, Int]) { (result, message) =>
        result + (message -> (result.getOrElse(message, 0) + 1))
      }
      .wireTap(
        Flow[CountResult]
          .throttle(1, 5 seconds, 0, Shaping)
          .map(printResult("Current result"))
          .to(Sink.ignore)
      )
      .runWith(Sink.last)

   // todo: sort output
   def printResult(title: String)(result: CountResult): Unit = {
    println("================================")
    println(s"$title")
    println("================================")
    result.foreach { case (word, count) =>
        println(s"$word: $count")
    }
  }

}

object WordSource {

  val SeparatorChars = Set('.', ',', '\n', ' ', '\t')

  def build(cr: CharacterReader): Source[String, NotUsed] = {

    // todo: rewrite as a stream stage?
    def nextWord: Option[String] = {
      @tailrec
      def loop(word: String): Option[String] =
        Try(cr.nextCharacter()) match {
          case Success(c) if SeparatorChars.contains(c) =>
            if (word.nonEmpty) Some(word) else loop("")
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
