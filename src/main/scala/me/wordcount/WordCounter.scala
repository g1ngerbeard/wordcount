package me.wordcount

import akka.NotUsed
import akka.stream.ThrottleMode.Shaping
import akka.stream.scaladsl.Sink.foreach
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, SubstreamCancelStrategy}
import me.wordcount.WordCounter.{CountResult, printResult, sortedResult}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordering
import scala.util.Try

object WordCounter {

  type CountResult = List[(String, Int)]

  def printResult(title: String)(result: CountResult): Unit = {
    println("================================")
    println(s"$title")
    println("================================")

    result.foreach { case (word, count) =>
      println(s"$word: $count")
    }
  }

  private def sortedResult(aggregate: Map[String, Int]): CountResult =
    aggregate
      .toList
      .sortBy(_.swap)(Ordering.Tuple2(Ordering[Int].reverse, Ordering[String]))

}

class WordCounter()(implicit mat: Materializer, ctx: ExecutionContext) {

  def count(first: CharacterReader, rest: CharacterReader*): Future[CountResult] = count(first :: rest.toList)

  //todo: async boundaries
  def count(readers: List[CharacterReader]): Future[CountResult] =
    Source(readers)
      .flatMapMerge(readers.length, WordSource.from(_).async)
      .scan(Map.empty[String, Int]) { (result, message) =>
        result + (message -> (result.getOrElse(message, 0) + 1))
      }
      .wireTap(
        Flow[Map[String, Int]]
          .throttle(1, 10 seconds, 0, Shaping)
          .map(sortedResult)
          .to(foreach(printResult("Current result")))
      )
      .runWith(Sink.last)
      .map(sortedResult)
}

object WordSource {

  val SeparatorChars = Set('.', ',', ' ', '!', '?', '\t', '\n')

  def from(cr: CharacterReader): Source[String, NotUsed] =
    Source
      .repeat(NotUsed)
      .map(_ => Try(cr.nextCharacter()).toOption)
      .takeWhile(_.nonEmpty, inclusive = true)
      .wireTap(elem => if (elem.isEmpty) cr.close())
      .collect { case Some(c) => c }
      .splitWhen(SubstreamCancelStrategy.propagate)(SeparatorChars.contains)
      .filterNot(SeparatorChars.contains)
      .fold(Vector.empty[Char])(_ :+ _)
      .map(_.mkString.toLowerCase)
      .filter(_.nonEmpty)
      .concatSubstreams

}
