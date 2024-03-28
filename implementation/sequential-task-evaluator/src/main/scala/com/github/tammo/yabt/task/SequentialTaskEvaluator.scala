package com.github.tammo.yabt.task

import com.github.tammo.yabt.ResolvedProject.ModuleReference
import com.github.tammo.yabt.task.Task.*
import com.github.tammo.yabt.task.Task.Result.*

object SequentialTaskEvaluator extends TaskEvaluator:

  // TODO optimize skipping of evaluation. Only evaluate if necessary.
  override def evaluateTask[T](task: Task[T], ctx: TaskContext): Result[T] =
    val aggregateResults: Set[Result[T]] =
      ctx.module.aggregates.map(moduleReference =>
        evaluateTaskForModule(moduleReference, task, ctx)
      )
    (aggregateResults + Success(task.evaluate(using ctx)))
      .foldLeft[Result[T]](Skipped) { case (first, second) =>
        first match
          case _: Success[T]     => second
          case failed: Failed[T] => failed
          case error: Error[T]   => error
          case Skipped           => second
      }

  private def evaluateTaskForModule[T](
      moduleReference: ModuleReference,
      task: Task[T],
      taskContext: TaskContext
  ): Result[T] =
    val module = taskContext.rootProject.modules.get(moduleReference)
    module match
      case Some(newModule) =>
        evaluateTask(task, taskContext.copy(module = newModule))
      case None => Failed(s"Aggregates undefined module $moduleReference")
