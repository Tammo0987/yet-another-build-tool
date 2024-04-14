package com.github.tammo.yabt.task.jvm

import com.github.tammo.yabt.ServiceProvider
import com.github.tammo.yabt.command.CommandDomain
import com.github.tammo.yabt.module.Module
import com.github.tammo.yabt.project.ProjectProvider
import com.github.tammo.yabt.task.TaskDSL.given
import com.github.tammo.yabt.task.jvm.tasks.{CleanTask, CompileTask}
import com.github.tammo.yabt.task.{Task, TaskEvaluator}

import scala.language.implicitConversions

object JVMModule extends Module:
  override def tasks(serviceProvider: ServiceProvider): Set[Task[?]] =
    Set(
      CleanTask.cleanTask,
      CompileTask(
        serviceProvider.dependencyResolver,
        serviceProvider.taskEvaluator
      ).compileTask
    )

  override def commands(
      serviceProvider: ServiceProvider
  ): Set[CommandDomain.Command[?]] =
    given TaskEvaluator = serviceProvider.taskEvaluator
    given ProjectProvider = serviceProvider.projectProvider

    Set(
      CleanTask.cleanTask,
      CompileTask(
        serviceProvider.dependencyResolver,
        serviceProvider.taskEvaluator
      ).compileTask
    )
