package com.github.tammo.yabt

import com.github.tammo.yabt.Error.*
import com.github.tammo.yabt.Error.ReadError.*
import com.github.tammo.yabt.ResolvedProject.ModuleReference
import com.github.tammo.yabt.cli.CommandLineInterface
import com.github.tammo.yabt.command.CommandDomain.Command
import com.github.tammo.yabt.module.ModuleDiscovery
import com.github.tammo.yabt.project.{ProjectReader, ProjectResolver}
import com.github.tammo.yabt.task.{TaskContext, TaskEvaluator}
import org.slf4j.LoggerFactory

import java.nio.file.Path

class CoreApplication(
    private val projectReader: ProjectReader,
    private val projectResolver: ProjectResolver,
    private val commandLineInterfaceProvider: Set[
      Command[?]
    ] => CommandLineInterface,
    private val moduleDiscovery: ModuleDiscovery,
    private val serviceProvider: ServiceProvider,
    private val taskEvaluator: TaskEvaluator
) {

  private lazy val logger = LoggerFactory.getLogger(getClass)

  def readProjectAndExecuteCommand(input: Array[String]): Unit = {
    val project = for {
      project <- projectReader.readProject()
      resolvedProject <- projectResolver.resolveProject(project)
    } yield resolvedProject

    project match
      case Left(resolveError) =>
        logResolveError(resolveError)
        return
      case Right(resolvedProject) =>
        logger.info(
          s"Project ${resolvedProject.name} in version ${resolvedProject.version} loaded."
        )

    moduleDiscovery.discoverModules match
      case Left(discoveryError) =>
        logger.error(discoveryError.message, discoveryError.throwable)
      case Right(modules) =>
        val commands = modules.flatMap(module =>
          module.commands ++ module.commands(serviceProvider)
        )
        val commandLineInterface = commandLineInterfaceProvider(commands)

        val recognizedTasks =
          modules.flatMap(module =>
            module.tasks ++ module.tasks(serviceProvider)
          )

        recognizedTasks
          .map(_.info.name)
          .foreach(taskName => logger.debug(s"Recognized task: $taskName"))

        // TODO remove
        val x = recognizedTasks.tail.head
        taskEvaluator.evaluateTask(x)(using
          TaskContext(
            Path.of(""),
            project.getOrElse(null),
            ModuleReference("/")
          )
        )

        logger.info(commandLineInterface.processArguments(input))
  }

  private def logResolveError(error: ResolveError): Unit = error match
    case FileError(message, underlying) =>
      logger.error(message, underlying)
    case ParseError(message, underlying) =>
      logger.error(message, underlying)
    case DecodingError(message, pathToRootString) =>
      logger.error(s"$message ${pathToRootString.getOrElse("")}")
    case MissingField(field) =>
      logger.error(s"Missing field $field")
    case MissingReference(reference) =>
      logger.error(s"Missing reference: $reference")
    case IllegalRootReference(path) =>
      logger.error(s"Illegal root reference found: ${path.mkString(" / ")}")
    case CyclicReference(path) =>
      logger.error(s"Cyclic reference found: ${path.mkString(" / ")}")

}
