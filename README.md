# scalatest-async-testing-comparison

ScalaTest enables asynchronous testing of `Futures` in various different ways.
This project explores the differences between them.
Also it attempts to answer Stack Overflow question [Scalatest Asynchronous Test Suites vs Eventually and WhenReady](https://stackoverflow.com/questions/55487833/scalatest-asynchronous-test-suites-vs-eventually-and-whenready-org-scalatest-co)

The following async testing facilities are explored:
* [Asynchronous style traits](http://www.scalatest.org/user_guide/async_testing), for example, [`AsyncFlatSpec`](http://doc.scalatest.org/3.0.0/index.html#org.scalatest.AsyncFlatSpec)
* [ScalaFutures](http://doc.scalatest.org/3.0.0/index.html#org.scalatest.concurrent.ScalaFutures)
* [Eventually](http://doc.scalatest.org/3.0.0/index.html#org.scalatest.concurrent.Eventually)


