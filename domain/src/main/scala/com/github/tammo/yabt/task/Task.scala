package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

case class Task[+T](
    info: Info,
    private[task] val computation: TaskContext => T
):
  def evaluate(using taskContext: TaskContext): T = computation(taskContext)

object Task:

  case class Info(name: String, description: String)

  enum Result[+T]:
    case Success(value: T)
    case Failed(message: String)
    private[task] case Error(message: String, throwable: Throwable)
    private[task] case Skipped
