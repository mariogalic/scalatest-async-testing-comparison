package example

import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class EventuallySpec extends FlatSpec with Eventually {
  behavior of "Eventually"
  note("is a more general concept than Async or ScalaFutures providing retrying capability on passed by-name parameter of any type")
  note("periodically retries executing a passed by-name parameter, until it either succeeds or the configured timeout has been surpassed.")

  override implicit val patienceConfig =
    PatienceConfig(timeout = Span(10, Seconds), interval = Span(20, Millis))

  it should "retry def Future.value: Option[Try[Int]]" in {
    val futureSum: Future[Int] = Future(1 + 2)
    eventually { assert(futureSum.value.contains(Success(3))) }
  }

  it should "retry def iterator: Iterator[Int]" in {
    val integers = 1 to 125
    val iterator = integers.iterator
    eventually { assert(iterator.next == 3) }
  }

  it should "retry def iterator: Iterator[Chars]" in {
    val alphabets = 'a' to 'z'
    val iterator = alphabets.iterator
    eventually { assert(iterator.next == 'p') }
  }

  it should "retry def randomNumberBetween1and10(): Int" in {
    eventually { assert(randomNumberBetween1and10() == 10) }
  }

  def randomNumberBetween1and10(): Int = {
    val r = new scala.util.Random
    1 + r. nextInt(10)
  }
}
