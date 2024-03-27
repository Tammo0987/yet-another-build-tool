package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

object SequentialTaskEvaluator extends TaskEvaluator:

  override def evaluateTask[T](task: Task[T])(using
      taskContext: TaskContext
  ): Result[T] =
    evaluateComputation(task.computation(taskContext))

  private def evaluateComputation[T](computation: Computation[T])(using
      taskContext: TaskContext
  ): Result[T] =
    computation match
      case pure: Pure[T]                => evaluatePure(pure)
      case compute: Compute[T]          => evaluateCompute(compute)
      case flatMapped: FlatMapped[_, T] => evaluateFlatMapped(flatMapped)
      case join: Join[T, _, _]          => evaluateJoin(join)
      case dependsOn: DependsOn[_, T]   => evaluateDependsOn(dependsOn)

  private def evaluatePure[T](pure: Pure[T]): Result[T] = Success(pure.value)

  private def evaluateCompute[T](compute: Compute[T]): Result[T] =
    Success(compute.f())

  private def evaluateFlatMapped[F, S](
      flatMapped: FlatMapped[F, S]
  )(using taskContext: TaskContext): Result[S] =
    evaluateTask(flatMapped.in) match
      case Success(value) => evaluateTask(flatMapped.f(value))
      case _              => Skipped

  private def evaluateJoin[T, F, S](
      join: Join[T, F, S]
  )(using taskContext: TaskContext): Result[T] =
    val Join(first, second, f) = join
    (evaluateTask(first), evaluateTask(second)) match
      case (Success(a), Success(b)) => evaluateTask(f(a, b))
      case _                        => Skipped

  private def evaluateDependsOn[F, T](
      dependsOn: DependsOn[F, T]
  )(using taskContext: TaskContext): Result[T] =
    evaluateTask(dependsOn.taskDependency)
    evaluateComputation(dependsOn.task.computation(taskContext))
