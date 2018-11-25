package com.michalplachta.influencerstats
import simulacrum.typeclass

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
      (list: List[A]) => list.length

    println(showIfNonEmpty(List(1, 2, 3)))
  }
}
