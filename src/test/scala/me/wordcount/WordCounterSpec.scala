package me.wordcount

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class WordCounterSpec extends FunSuite with Matchers {

  val SimpleText = "bla bla, one two. Three bla weee bla one bla two day green black friday day monday green lake seven sky"

  val SimpleText2 = "What is the action of following approach. Two three bla weee blagreen lake seven sky, one bla two. day green monday "

  test("count words in simple text"){
    val delayedReader = new DelayedCharacterReader(SimpleText)
    val delayedReader2 = new DelayedCharacterReader(SimpleText2)

    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()

    val result = Await.result(WordCounter.count(delayedReader, delayedReader2), 100 seconds)

    WordCounter.printResult("Final result")(result)

    system.terminate()
  }

//  test("construct source") {
//    val delayedReader = new DelayedCharacterReader(SimpleText)
//
//    implicit val system: ActorSystem = ActorSystem()
//    implicit val mat: ActorMaterializer = ActorMaterializer()
//
//    val result = WordSource.build(delayedReader).runReduce(_ + " " + _)
//
//    Await.result(result, 10 seconds) shouldBe SimpleText
//
//  }

}
