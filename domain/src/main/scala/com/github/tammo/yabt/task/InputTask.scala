package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.{Result, TaskInfo}

case class InputTask[I, +T](
    info: TaskInfo,
    defaultInput: I,
    private[task] val action: I => TaskContext => Result[T]
)
