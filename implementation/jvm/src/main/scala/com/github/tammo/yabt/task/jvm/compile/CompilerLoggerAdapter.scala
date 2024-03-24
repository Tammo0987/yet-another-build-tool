package com.github.tammo.yabt.task.jvm.compile

import org.slf4j.LoggerFactory
import xsbti.Logger

import java.util.function.Supplier

object CompilerLoggerAdapter extends Logger:

  private val logger = LoggerFactory.getLogger(getClass)

  override def error(msg: Supplier[String]): Unit =
    logger.error(msg.get())

  override def warn(msg: Supplier[String]): Unit =
    logger.warn(msg.get())

  override def info(msg: Supplier[String]): Unit =
    logger.info(msg.get())

  override def debug(msg: Supplier[String]): Unit =
    logger.debug(msg.get())

  override def trace(exception: Supplier[Throwable]): Unit =
    val error = exception.get()
    logger.trace(error.getMessage, error)
