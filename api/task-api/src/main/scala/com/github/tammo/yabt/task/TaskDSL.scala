package com.github.tammo.yabt.task

import com.github.tammo.yabt.command.CommandDomain.{Command, CommandExecutionResult, Option}
import com.github.tammo.yabt.project.ProjectProvider
import com.github.tammo.yabt.task.Task.*

import java.nio.file.Paths
import scala.annotation.targetName

object TaskDSL:

  def task[T](name: String, description: String)(
      action: TaskContext => Task.Result[T]
  ): Task[T] =
    Task(TaskInfo(name, description), action)

  @targetName("taskConvenient")
  def task[T](name: String, description: String)(
      action: TaskContext => T
  ): Task[T] =
    task(name, description)(action.andThen(Task.Result.Success.apply))

  def inputTask[I, T](name: String, description: String, defaultInput: I)(
      action: I => TaskContext => Task.Result[T]
  ): InputTask[I, T] =
    InputTask(TaskInfo(name, description), defaultInput, action)

  // TODO generate this later with a macro
  private type TaskInputToCommandOptions[I] = () => Set[Option[I, ?]]

  // TODO find the right place for this function. Probably the command executor?
  given [I, T](using
      transformer: TaskInputToCommandOptions[I],
      taskEvaluator: TaskEvaluator,
      projectProvider: ProjectProvider
  ): Conversion[InputTask[I, T], Command[I]] = inputTask =>
    new Command[I](
      inputTask.info.name,
      inputTask.info.description,
      transformer(),
      inputTask.defaultInput,
      input =>
        val task = Task(inputTask.info, inputTask.action(input))
        val taskContext = TaskContext(
          Paths.get(""),
          projectProvider.project,
          projectProvider.project.toModule
        )
        taskEvaluator.evaluateTask(task, taskContext) match
          case Result.Success(value)  => CommandExecutionResult.Success
          case Result.Failed(message) => CommandExecutionResult.Error
          case Result.Error(message, throwable) => CommandExecutionResult.Error
          case Result.Skipped                   => CommandExecutionResult.Error
    )

  given [T](using
      taskEvaluator: TaskEvaluator,
      projectProvider: ProjectProvider
  ): Conversion[Task[T], Command[Unit]] = task =>
    new Command[Unit](
      task.info.name,
      task.info.description,
      Set.empty,
      (),
      _ =>
        val taskContext = TaskContext(
          Paths.get(""),
          projectProvider.project,
          projectProvider.project.toModule
        )
        taskEvaluator.evaluateTask(task, taskContext) match
          case success: Task.Result.Success[T] => CommandExecutionResult.Success
          case Result.Failed(message)          => CommandExecutionResult.Error
          case Result.Error(message, throwable) =>
            CommandExecutionResult.Error
          case Result.Skipped => CommandExecutionResult.Error
    )
