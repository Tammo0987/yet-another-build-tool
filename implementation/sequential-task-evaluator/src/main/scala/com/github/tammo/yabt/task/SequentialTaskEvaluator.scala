package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

object SequentialTaskEvaluator extends TaskEvaluator:

  override def evaluateTask[T](task: Task[T], ctx: TaskContext): Result[T] =
    Result.Success(task.evaluate(using ctx))
