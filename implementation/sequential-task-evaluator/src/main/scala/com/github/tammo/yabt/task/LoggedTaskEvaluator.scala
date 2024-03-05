package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

trait LoggedTaskEvaluator extends TaskEvaluator:
  abstract override def evaluateTask[T](task: Task[T])(using
      ctx: TaskContext
  ): Task.Result[T] = {
    val result = super.evaluateTask(task)
    val prefix = resultToPrefix(result)
    println(s"[$prefix] Evaluated task ${task.info.name} with result: $result")

    result
  }

  private def resultToPrefix[T](result: Result[T]) = {
    val prefix = result match
      case _: Success[T] => "Success"
      case _: Failed     => "Failed"
      case _: Error      => "Error"
      case Skipped       => "Skipped"
    prefix
  }
