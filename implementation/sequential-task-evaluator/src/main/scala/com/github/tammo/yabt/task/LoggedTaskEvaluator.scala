package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*
import com.github.tammo.yabt.task.Task.Result.*
import org.slf4j.LoggerFactory

trait LoggedTaskEvaluator extends TaskEvaluator:

  private lazy val logger = LoggerFactory.getLogger(getClass)

  abstract override def evaluateTask[T](
      task: Task[T],
      ctx: TaskContext
  ): Result[T] =
    val result = super.evaluateTask(task, ctx)

    result match
      case _: Success[T] =>
        logger.info(s"Finished task ${task.info.name}.")
      case Failed(message) =>
        logger.info(s"Task failed: $message")
      case Error(message, throwable) =>
        logger.error(s"Task error: $message", throwable)
      case Skipped =>
        logger.info(s"Skipped task.")

    result
