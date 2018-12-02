package me.wordcount

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import me.wordcount.WordCounterSpec.{withMaterializer, withWordCounter}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class WordCounterSpec extends FunSuite with Matchers {

  test("construct source of words") {
    withMaterializer { implicit mat =>
      val testText = "Hello, mister cat! What is your name?"
      val words = List("hello", "mister", "cat", "what", "is", "your", "name")

      val reader = new SimpleCharacterReader(testText)

      val result = WordSource.build(reader).runFold(Vector.empty[String])(_ :+ _)
      Await.result(result, 1 second) shouldBe words
    }
  }

  test("count words in text") {
    withWordCounter { wordCounter =>
      val reader = new DelayedCharacterReader("The cat sat on the mat", 10)
      val output = List("the" -> 2, "cat" -> 1, "mat" -> 1, "on" -> 1, "sat" -> 1)

      val result = Await.result(wordCounter.count(reader), 1 second)

      result shouldBe output
    }
  }

  ignore("count words from two sources") {
    withWordCounter { wordCounter =>
      val SimpleText = "bla bla, one two. Three bla weee bla one bla two day green black friday day monday green lake seven sky"
      val SimpleText2 = "What is the action of following approach. Two three bla weee blagreen lake seven sky, one bla two. day green monday "

      val delayedReader = new DelayedCharacterReader(SimpleText, 50)
      val delayedReader2 = new DelayedCharacterReader(SimpleText2, 50)

      val result = Await.result(wordCounter.count(delayedReader, delayedReader2), 10 seconds)

      WordCounter.printResult("Final result")(result)
    }
  }

}

object WordCounterSpec {

  def withMaterializer(runTest: ActorMaterializer => Unit): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    val mat: ActorMaterializer = ActorMaterializer()

    try {
      runTest(mat)
    } finally {
      Await.ready(system.terminate(), 1 second)
    }
  }

  def withWordCounter(runTest: WordCounter => Unit): Unit =
    withMaterializer { implicit mat =>
      runTest(new WordCounter())
    }

}
