package com.github.tammo.yabt.task.jvm

import com.github.tammo.yabt.command.CommandDomain
import com.github.tammo.yabt.module.Module
import com.github.tammo.yabt.task.Task
import com.github.tammo.yabt.task.jvm.tasks.CleanTask

object JVMModule extends Module:
  override def tasks: Set[Task[?]] = Set(CleanTask.cleanTask)

  override def commands: Set[CommandDomain.Command[?]] = super.commands
