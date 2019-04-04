package example

import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ScalaFuturesSpec extends FlatSpec with ScalaFutures {
  behavior of "ScalaFutures"

  var mutableSharedState = 0
  it should "not guarantee thread-safety" in {
    info("use global execution context backed by a multi-threaded pool")
    info("does NOT guarantee tasks will execute one after another in the order they were started")
    val f1 = Future {
      val tmp = mutableSharedState
      Thread.sleep(5000)
      println(s"Start Future 1 with mutableSharedState=$tmp")
      mutableSharedState = tmp + 1
      println(s"Complete Future 1 with mutableSharedState=$mutableSharedState")
    }

    val f2 = Future {
      val tmp = mutableSharedState
      println(s"Start Future 2 with mutableSharedState=$tmp")
      mutableSharedState = tmp + 1
      println(s"Complete Future 2 with mutableSharedState=$mutableSharedState")
    }

    val f = for {
      _ <- f1
      _ <- f2
    } yield {
      assert(mutableSharedState == 2)
    }

    implicit val patienceConfig = PatienceConfig(timeout = Span(10, Seconds), interval = Span(20, Millis))
    whenReady(f){identity}
  }

  it should "takes into account all the assertions" in {
    val futureSum: Future[Int] = Future(1 + 2)
    whenReady(futureSum) { sum => assert(sum == 4) }
    whenReady(futureSum) { sum => assert(sum == 3) }
  }

}
