package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

case class Task[+T](info: Info, computation: Computation[T])

object Task:

  case class Info(parallel: Boolean)

  sealed trait Result[+T]

  final case class Success[+T](value: T) extends Result[T]

  final case class Failed(message: String) extends Result[Nothing]

  final case class Error(message: String, throwable: Throwable)
      extends Result[Nothing]

  case object Skipped extends Result[Nothing]

  sealed trait Computation[+T]:

    def evaluate(): Result[T] = this match
      case Pure(value) => Success(value)
      case FlatMapped(in, f) =>
        in.evaluate() match
          case Success(value) => f(value).evaluate()
          case Failed(message) =>
            println(message)
            Skipped
          case Error(message, throwable) =>
            println(s"$message $throwable")
            Skipped
          case Skipped => Skipped
      case Join(first, second, f) =>
        (first.evaluate(), second.evaluate()) match
          case (Success(a), Success(b)) => f(a, b).evaluate()
          case _                        => Skipped

  final case class Pure[+T](value: T) extends Computation[T]

  final case class FlatMapped[F, +T](
      in: Computation[F],
      f: F => Computation[T]
  ) extends Computation[T]

  final case class Join[+T, A, B](
      first: Computation[A],
      second: Computation[B],
      f: (A, B) => Computation[T]
  ) extends Computation[T]
