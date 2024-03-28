package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.*

object TaskDSL:

  def task[T](name: String, description: String)(
      computation: TaskContext => T
  ): Task[T] =
    Task(Info(name, description), computation)
