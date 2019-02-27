package org.scalacheck
package instances

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

import cats._
import cats.implicits._

import Gen.Parameters
import rng.Seed

object gen extends GenInstances {

  def sampledEq[A: Eq](trials: Int): Eq[Gen[A]] =
    new Eq[Gen[A]] {
      def eqv(x: Gen[A], y: Gen[A]): Boolean = {
        val params = Gen.Parameters.default
        def loop(count: Int, seed: Seed): Boolean =
          if (count <= 0) true else {
            val tx = Try(x.doApply(params, seed))
            val ty = Try(y.doApply(params, seed))
            (tx, ty) match {
              case (Failure(_), Failure(_)) =>
                loop(count - 1, Seed.random)
              case (Success(rx), Success(ry)) =>
                if (rx.retrieve != ry.retrieve) false
                else loop(count - 1, seed.next)
              case _ =>
                println("nope3!")
                false
            }
          }
        loop(trials, Seed.random)
      }
    }
}

trait GenInstances extends GenInstances0 {

  implicit def genMonoid[A: Monoid]: Monoid[Gen[A]] =
    new Monoid[Gen[A]] {
      val empty: Gen[A] =
        Gen.const(Monoid[A].empty)
      def combine(gx: Gen[A], gy: Gen[A]): Gen[A] =
        for { x <- gx; y <- gy } yield x |+| y
    }

  implicit val genAlternative: Alternative[Gen] =
    new Alternative[Gen] {
      override def ap[A, B](ff: Gen[A => B])(fa: Gen[A]): Gen[B] =
        for { ff <- ff; fa <- fa } yield ff(fa)
      override def empty[A]: Gen[A] =
        Gen.fail
      override def combineK[A](gx: Gen[A], gy: Gen[A]): Gen[A] =
        Gen.gen { (params, seed) =>
          val rx = gx.doApply(params, seed)
          if (rx.retrieve.isDefined) rx
          else gy.doApply(params, rx.seed)
        }
      override def pure[A](a: A): Gen[A] =
        Gen.const(a)
      override def map[A, B](g: Gen[A])(f: A => B): Gen[B] =
        g.map(f)
    }
}

trait GenInstances0 {
  implicit def genSemigroup[A: Semigroup]: Semigroup[Gen[A]] =
    new Semigroup[Gen[A]] {
      def combine(gx: Gen[A], gy: Gen[A]): Gen[A] =
        for { x <- gx; y <- gy } yield x |+| y
    }
}
