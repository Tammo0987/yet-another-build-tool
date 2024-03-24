package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*
import org.slf4j.LoggerFactory

trait LoggedTaskEvaluator extends TaskEvaluator:

  private lazy val logger = LoggerFactory.getLogger(getClass)

  abstract override def evaluateTask[T](task: Task[T])(using
      ctx: TaskContext
  ): Task.Result[T] = {
    val result = super.evaluateTask(task)

    result match
      case _: Success[T] =>
        logger.info("Finished task.")
      case Failed(message) =>
        logger.info(s"Task failed: $message")
      case Error(message, throwable) =>
        logger.error(s"Task error: $message", throwable)
      case Task.Skipped =>
        logger.info(s"Skipped task.")

    result
  }
