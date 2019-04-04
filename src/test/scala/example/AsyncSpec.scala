package example

import org.scalatest.AsyncFlatSpec
import scala.concurrent.Future


/**
  * ScalaTest provides its serial execution context as the default on the JVM for three reasons.
  *  - First, most often running both tests and suites in parallel does not give a significant performance boost compared to just running suites in parallel.
  *  - Second, if multiple threads are operating in the same suite concurrently, you'll need to make sure access to any mutable fixture objects by multiple threads is synchronized.
  *  - Third, asynchronous-style tests need not be complete when the test body returns, because the test body returns a Future[Assertion].
  */
class AsyncSpec extends AsyncFlatSpec {
  behavior of "Async Test Styles"
  note("http://www.scalatest.org/user_guide/async_testing")

  var mutableSharedState = 0
  it should "guarantee thread-safety" in {
    info("use ScalaTest provided asynchronous execution model implementing single-threaded serial execution")
    info("guarantees tasks will execute one after another in the order they were enqueued")
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
