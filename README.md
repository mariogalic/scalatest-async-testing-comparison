# ScalaTest-ing Futures with Asynchronous style traits vs ScalaFutures vs Eventually 

ScalaTest supports many different ways of testing code that returns `Future`.
This project explores the differences between them.
Also it attempts to answer Stack Overflow question [Scalatest Asynchronous Test Suites vs Eventually and WhenReady](https://stackoverflow.com/questions/55487833/scalatest-asynchronous-test-suites-vs-eventually-and-whenready-org-scalatest-co)

The following async testing facilities are explored:
* [Asynchronous style traits](http://www.scalatest.org/user_guide/async_testing), for example, [`AsyncFlatSpec`](http://doc.scalatest.org/3.0.0/index.html#org.scalatest.AsyncFlatSpec)
* [ScalaFutures](http://doc.scalatest.org/3.0.0/index.html#org.scalatest.concurrent.ScalaFutures)
* [Eventually](http://doc.scalatest.org/3.0.0/index.html#org.scalatest.concurrent.Eventually)

## Asynchronous style traits

```
class AsyncSpec extends AsyncFlatSpec {
  ...
  Future(3).map { v => assert(v == 3) }
  ...
}
```
 
* non-blocking
* we can assert before `Future` completes, i.e., return `Future[Assertion]` instead of `Assertion`
* thread-safe
* single-threaded serial execution context
* `Futures` execute and complete in the order they are started and one after another
* the same thread that is used to enqueue tasks in the test body is also used to execute them afterwards 
* Assertions can be mapped over `Futures`
* no need to block inside the test body, i.e., use `Await`, `whenReady`
* eliminates flakiness due to thread starvation
* last expression in the test body must be `Future[Assertion]`
* does not support multiple assertions in the test body
* cannot use blocking constructs inside the test body as it will hang the test forever because of waiting on
enqueued but never started task

## ScalaFutures

```
class ScalaFuturesSpec extends FlatSpec with ScalaFutures {
  ...
  whenReady(Future(3) { v => assert(v == 3) }
  ...
}
```

* blocking
* we must wait to complete the `Future` before we can return `Assertion`
* not thread-safe
* Likely to be used with global execution context `scala.concurrent.ExecutionContext.Implicits.global` which is a
multi-threaded pool for parallel execution
* supports multiple assertions within the same test body 
* last expression in the test body does not have to be `Assertion`

## Eventually

```
class EventuallySpec extends FlatSpec with Eventually {
  ...
  eventually { assert(Future(3).value.contains(Success(3))) }
  ...
}
```

* more general facility intended not just for `Futures`
* semantics here are that of retrying a block of code of any type passed in by-name until assertion is satisfied
* when testing `Futures` it is likely global execution context will be used 
* intended primarily for integration testing where testing against real services with unpredictable response times

## Single-threaded serial execution model vs. thread-pooled global execution model

[scalatest-async-testing-comparison](https://github.com/mario-galic/scalatest-async-testing-comparison) is an example
demonstrating the difference in two execution model. 

Given the following test body

```
    val f1 = Future {
      val tmp = mutableSharedState
      Thread.sleep(5000)
      println(s"Start Future1 with mutableSharedState=$tmp in thread=${Thread.currentThread}")
      mutableSharedState = tmp + 1
      println(s"Complete Future1 with mutableSharedState=$mutableSharedState")
    }

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

```

let us consider the output of `AsyncSpec` against `ScalaFuturesSpec`

* testOnly example.AsyncSpec:

    ```
    Start Future1 with mutableSharedState=0 in thread=Thread[pool-11-thread-3-ScalaTest-running-AsyncSpec,5,main]
    Complete Future1 with mutableSharedState=1
    Start Future2 with mutableSharedState=1 in thread=Thread[pool-11-thread-3-ScalaTest-running-AsyncSpec,5,main]
    Complete Future2 with mutableSharedState=2
    ```

* testOnly example.ScalaFuturesSpec:

    ```
    Start Future2 with mutableSharedState=0 in thread=Thread[scala-execution-context-global-119,5,main]
    Complete Future2 with mutableSharedState=1
    Start Future1 with mutableSharedState=0 in thread=Thread[scala-execution-context-global-120,5,main]
    Complete Future1 with mutableSharedState=1
    ```

Note how in serial execution model same thread is used and Futures completed in order. On the other hand, 
in global execution model different threads were used, and `Future2` completed before `Future1`, which caused
race condition on the shared mutable state, which in turn made the test fail.


## Which one should we use (IMO)?

In unit tests we should use mocked subsystems where returned `Futures` should be completing near-instantly, so there
is no need for `Eventually` in unit tests. Hence the choice is between async styles and `ScalaFutures`. The main difference
between the two is that former is non-blocking unlike the latter. If possible, we should never block, so we
should prefer async styles like `AsyncFlatSpec`. Further big difference is the execution model. Async styles 
by default use custom serial execution model which provides thread-safety on shared mutable state, unlike global
thread-pool backed execution model often used with `ScalaFutures`. In conclusion, my suggestion is we use async style
traits unless we have a good reason not to.


