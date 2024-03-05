package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

object TaskDSL {

  // def task[T](name: String, description: String)(f: TaskContext => T): Task[T] =
//    Task(Info(name, description), f.andThen(g => Compute(() => g)))

  def task[T](name: String, description: String)(
      computation: TaskContext => Computation[T]
  ): Task[T] =
    Task(Info(name, description), computation)

/*  def task[T](name: String, description: String)(
      computation: Computation[T]
  ): Task[T] =
    Task(Info(name, description), _ => computation)


 */
  def sequence[F, S](
      first: Task[F],
      second: F => Task[S]
  ): Computation[S] =
    FlatMapped[F, S](first, f => second(f))

  extension [T](task: Task[T]) {

    def dependsOn[B](other: Task[B]): Task[T] =
      Task(task.info, _ => DependsOn(task, other))

  }
}
