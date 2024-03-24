package com.github.tammo.yabt.task.jvm

import com.github.tammo.yabt.ServiceProvider
import com.github.tammo.yabt.command.CommandDomain
import com.github.tammo.yabt.module.Module
import com.github.tammo.yabt.task.Task
import com.github.tammo.yabt.task.jvm.tasks.{CleanTask, CompileTask}

object JVMModule extends Module:
  override def tasks(serviceProvider: ServiceProvider): Set[Task[?]] =
    Set(
      CleanTask.cleanTask,
      CompileTask(serviceProvider.dependencyResolver).compileTask
    )

  override def commands: Set[CommandDomain.Command[?]] = super.commands
