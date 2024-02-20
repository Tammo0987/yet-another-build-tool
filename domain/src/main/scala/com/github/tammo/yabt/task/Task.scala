package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

case class Task[+T](info: Info, computation: TaskContext => Computation[T])

private[task] object Task:

  case class Info(name: String, description: String)

  sealed trait Result[+T]

  final case class Success[+T](value: T) extends Result[T]

  final case class Failed(message: String) extends Result[Nothing]

  final case class Error(message: String, throwable: Throwable)
      extends Result[Nothing]

  case object Skipped extends Result[Nothing]

  sealed trait Computation[+T]

  final case class Pure[+T](value: T) extends Computation[T]

  final case class Compute[+T](f: () => T) extends Computation[T]

  final case class FlatMapped[F, +T](
      in: Task[F],
      f: F => Task[T]
  ) extends Computation[T]

  final case class DependsOn[F, +T](
      task: Task[T],
      taskDependency: Task[F]
  ) extends Computation[T]

  final case class Join[+T, A, B](
      first: Task[A],
      second: Task[B],
      f: (A, B) => Task[T]
  ) extends Computation[T]
