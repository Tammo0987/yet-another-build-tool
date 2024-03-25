package com.github.tammo.yabt.task.jvm.compile

import org.slf4j.LoggerFactory
import xsbti.{Position, Problem, Reporter, Severity}

object CompilerReporter extends Reporter:

  private lazy val logger = LoggerFactory.getLogger(getClass)

  override def reset(): Unit = ()

  override def hasErrors: Boolean = false

  override def hasWarnings: Boolean = false

  override def printSummary(): Unit = ()

  override def problems(): Array[Problem] = Array.empty

  override def log(problem: Problem): Unit = {
    problem.severity() match
      case Severity.Info =>
        logger.info(problem.message())
      case Severity.Warn  =>
        logger.warn(problem.message())
      case Severity.Error =>
        logger.error(problem.position().toString)
        logger.error(problem.message())
  }

  override def comment(pos: Position, msg: String): Unit = ()
