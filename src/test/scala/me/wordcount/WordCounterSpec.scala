package me.wordcount

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import me.wordcount.WordCounterSpec.{TestDelay, withMaterializer, withWordCounter}
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class WordCounterSpec extends FunSuite with Matchers {

  test("construct source of words") {
    withMaterializer { implicit mat =>
      val testText = "Hello, mister cat! What is your name?"
      val words = List("hello", "mister", "cat", "what", "is", "your", "name")

      val reader = new SimpleCharacterReader(testText)

      val result = WordSource.from(reader).runFold(Vector.empty[String])(_ :+ _)
      Await.result(result, 1 second) shouldBe words
    }
  }

  test("count words in text") {
    withWordCounter { wordCounter =>
      val reader = new DelayedCharacterReader("The cat sat on the mat", TestDelay)

      val result = Await.result(wordCounter.count(reader), 1 second)

      result shouldBe List("the" -> 2, "cat" -> 1, "mat" -> 1, "on" -> 1, "sat" -> 1)
    }
  }

  test("count words from multiple sources") {
    withWordCounter { wordCounter =>
      val TestText1 = "The cat sat on the mat"
      val TestText2 = "The dog sat on the stone"

      val reader1 = new DelayedCharacterReader(TestText1, TestDelay)
      val reader2 = new DelayedCharacterReader(TestText2, TestDelay * 3)

      val result = Await.result(wordCounter.count(reader1, reader2), 1 second)

      result shouldBe List("the" -> 4, "on" -> 2, "sat" -> 2, "cat" -> 1, "dog"-> 1, "mat" -> 1, "stone" -> 1)
    }
  }

}

object WordCounterSpec {

  val TestDelay = 10

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
