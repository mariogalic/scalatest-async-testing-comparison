package example

import org.scalatest.AsyncFlatSpec
import scala.concurrent.Future

class AsyncSpec extends AsyncFlatSpec {
  behavior of "Async Test Styles"

  var mutableSharedState = 0
  it should "guarantee thread-safety" in {
    val f1 = Future {
      val tmp = mutableSharedState
      Thread.sleep(5000)
      println(s"Start Future1 with mutableSharedState=$tmp in thread=${Thread.currentThread}")
      mutableSharedState = tmp + 1
      println(s"Complete Future1 with mutableSharedState=$mutableSharedState")
    }

    // Await.result(f1, Duration.Inf) // WARNING: This would hang the test forever serial execution model

    val f2 = Future {
      val tmp = mutableSharedState
      println(s"Start Future2 with mutableSharedState=$tmp in thread=${Thread.currentThread}")
      mutableSharedState = tmp + 1
      println(s"Complete Future2 with mutableSharedState=$mutableSharedState")
    }

    for {
      _ <- f1
      _ <- f2
    } yield {
      assert(mutableSharedState == 2)
    }
  }

  it should "take into account only the last assertion" in {
    info("https://medium.com/@hussachai/beware-the-asyn-test-35c5c848ebe9")
    val futureSum: Future[Int] = Future(1 + 2)
    futureSum map { sum => fail() } // WARNING: assertion result is discarded
    futureSum map { sum => assert(sum == 3) }
  }
}
