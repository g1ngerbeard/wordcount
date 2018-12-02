package me.wordcount

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import me.wordcount.WordCounter.CountResult

import scala.concurrent.Await
import scala.concurrent.duration._

object TestApp extends App {

  val TestText =
    """Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem
      Ipsum has been the industry's standard dummy text ever since the 1500s, when
      an unknown printer took a galley of type and scrambled it to make a type
      specimen book. It has survived not only five centuries, but also the leap
      into electronic typesetting, remaining essentially unchanged. It was popularised
      in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages,
      and more recently with desktop publishing software like Aldus PageMaker including
      versions of Lorem Ipsum. It is a long established fact that a reader will be distracted
      by the readable content of a page when looking at its layout. The point of using Lorem
      Ipsum is that it has a more-or-less normal distribution"""

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val readers = TestText.split('\n').map(new DelayedCharacterReader(_,1000)).toList

  val result: CountResult = Await.result(new WordCounter().count(readers), 120 seconds)

  WordCounter.printResult("Final total:")(result)

  system.terminate()
}
