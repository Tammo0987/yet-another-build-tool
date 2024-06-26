package com.github.tammo.yabt.task

import com.github.tammo.yabt.task.Task.Result

trait TaskEvaluator:

  def evaluateTask[T](task: Task[T], ctx: TaskContext): Result[T]
