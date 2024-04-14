package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

case class Task[+T](
    info: TaskInfo,
    private[task] val action: TaskContext => Result[T]
)

object Task:

  case class TaskInfo(name: String, description: String)

  enum Result[+T]:
    case Success(value: T)
    case Failed(message: String)
    private[task] case Error(message: String, throwable: Throwable)
    private[task] case Skipped
