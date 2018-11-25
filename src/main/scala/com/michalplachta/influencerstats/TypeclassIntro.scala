package com.michalplachta.influencerstats
import cats.Monad
import cats.effect.IO
import cats.implicits._
import com.michalplachta.influencerstats.logging.{DefaultLogger, FutureLogger, Logging}
import simulacrum.typeclass

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object TypeclassIntro {
  object imperative {
    trait Show {
      def show: String
    }

    trait Length {
      def length: Int
    }

    def showIfNonEmpty(x: Show with Length): String = {
      if (x.length > 0) x.show
      else "EMPTY"
    }

    class ListShowLength(list: List[_]) extends Show with Length {
      override def show   = list.toString
      override def length = list.length
    }

    println(showIfNonEmpty(new ListShowLength(List(1, 2, 3))))
  }

  object functional {
    @typeclass
    trait Show[A] {
      def show(a: A): String
    }

    @typeclass
    trait Length[A] {
      def length(a: A): Int
    }

    def showIfNonEmpty[A: Show: Length](a: A): String = {
      if (Length[A].length(a) > 0) Show[A].show(a)
      else "EMPTY"
    }

    implicit def showList[A]: Show[List[A]] =
      (list: List[A]) => list.toString

    implicit def lengthList[A]: Length[List[A]] =
      (list: List[A]) => list.size

    println(showIfNonEmpty(List(1, 2, 3)))
  }

  // ESSENTIAL COMPLEXITY
  def addInF[F[_]: Monad: Logging](fa: F[Int], fb: F[Int]): F[Int] = { // reduced degree of freedom
    for {
      a <- fa
      b <- fb
      _ <- Logging[F].info(s"adding $a and $b")
    } yield a + b
  } // this function can just flatMap and log - nothing more (don't have to look at implementation!)

  // ACCIDENTAL COMPLEXITY
  implicit val ioLogging     = new DefaultLogger[IO]
  implicit val futureLogging = new FutureLogger

  val readInFuture: Future[Int]   = Future(scala.io.StdIn.readInt()) // Future effect: starts on creation
  val resultInFuture: Future[Int] = addInF(readInFuture, readInFuture)
  println(Await.result(resultInFuture, 5.seconds))

  // REFERENTIAL TRANSPARENCY FTW!
  val readInIO: IO[Int]   = IO(scala.io.StdIn.readInt()) // IO effect: don't start on creation
  val resultInIO: IO[Int] = addInF(readInIO, readInIO)
  println(resultInIO.unsafeRunSync())
}
