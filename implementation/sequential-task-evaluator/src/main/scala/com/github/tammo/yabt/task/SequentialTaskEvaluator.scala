package com.github.tammo.yabt.task

import com.github.tammo.yabt.ResolvedProject.ModuleReference
import com.github.tammo.yabt.task.Task.*
import com.github.tammo.yabt.task.Task.Result.*

object SequentialTaskEvaluator extends TaskEvaluator:

  override def evaluateTask[T](task: Task[T], ctx: TaskContext): Result[T] =
    import TaskEvaluation.*

    val tasksToExecute: Seq[TaskEvaluation] = TaskToEvaluate(ctx) +:
      ctx.module.aggregates.map(moduleReferenceToTaskEvaluation(_, ctx))

    tasksToExecute.foldRight[Option[Result[T]]](None):
      case (taskEvaluation, previousResult) =>
        previousResult match
          case skipped @ Some(Skipped)   => skipped
          case error @ Some(Error(_, _)) => error
          case failed @ Some(Failed(_))  => failed
          case _ =>
            taskEvaluation match
              case FailedBeforeEvaluation(message) => Some(Failed(message))
              case TaskToEvaluate(taskContext) if taskContext == ctx =>
                Some(Success(task.evaluate(using taskContext)))
              case TaskToEvaluate(taskContext) =>
                Option(evaluateTask(task, taskContext))
    match
      case Some(result) => result
      case None         => Failed(s"No result for task ${task.info.name}.")

  private enum TaskEvaluation:
    case FailedBeforeEvaluation(message: String)
    case TaskToEvaluate(taskContext: TaskContext)

  private def moduleReferenceToTaskEvaluation(
      moduleReference: ModuleReference,
      taskContext: TaskContext
  ): TaskEvaluation =
    taskContext.rootProject.modules.get(moduleReference) match
      case None =>
        TaskEvaluation.FailedBeforeEvaluation(
          s"Aggregates references undefined module $moduleReference."
        )
      case Some(module) =>
        TaskEvaluation.TaskToEvaluate(taskContext.copy(module = module))
