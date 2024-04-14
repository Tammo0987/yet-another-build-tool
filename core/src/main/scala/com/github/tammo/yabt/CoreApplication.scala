package com.github.tammo.yabt

import com.github.tammo.yabt.CoreApplication.{CoreError, Dependencies}
import com.github.tammo.yabt.Resolve.ResolveError
import com.github.tammo.yabt.ResolvedProject.ResolvedProject
import com.github.tammo.yabt.cli.CommandLineInterface
import com.github.tammo.yabt.command.CommandDomain.Command
import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.module.ModuleDiscovery
import com.github.tammo.yabt.module.ModuleDiscovery.DiscoveryError
import com.github.tammo.yabt.project.{ProjectProvider, ProjectReader, ProjectResolver}
import com.github.tammo.yabt.task.TaskEvaluator
import org.slf4j.LoggerFactory

class CoreApplication(private val dependencies: Dependencies):

  private lazy val logger = LoggerFactory.getLogger(getClass)

  def main(input: Array[String]): Either[CoreError, Unit] =
    import dependencies.*

    for {
      project <- projectReader.readProject()
      resolvedProject <- projectResolver.resolveProject(project)
      _ = logger.info(
        s"Project ${resolvedProject.name} in version ${resolvedProject.version} loaded."
      )
      modules <- moduleDiscovery.discoverModules
      serviceProvider = createServiceProvider(resolvedProject)
      commands = modules.flatMap(module =>
        module.commands ++ module.commands(serviceProvider)
      )
      tasks = modules.flatMap(module =>
        module.tasks ++ module.tasks(serviceProvider)
      )
      commandLineInterface = commandLineInterfaceProvider(
        commands
      )

    } yield
      if input.isEmpty then
        new CommandLineInputLoop(commandLineInterface).loop()
      else commandLineInterface.processArguments(input); ()

  private def createServiceProvider(
      resolvedProject: ResolvedProject
  ): ServiceProvider = new ServiceProvider:
    override def dependencyResolver: DependencyResolver =
      dependencies.dependencyResolver

    override def projectProvider: ProjectProvider = new ProjectProvider:
      override def project: ResolvedProject = resolvedProject

    override def taskEvaluator: TaskEvaluator =
      dependencies.taskEvaluator

object CoreApplication:

  case class Dependencies(
      projectReader: ProjectReader,
      projectResolver: ProjectResolver,
      commandLineInterfaceProvider: CommandLineInterfaceProvider,
      moduleDiscovery: ModuleDiscovery,
      taskEvaluator: TaskEvaluator,
      dependencyResolver: DependencyResolver
  )

  private type CommandLineInterfaceProvider =
    Set[Command[?]] => CommandLineInterface

  type CoreError = ResolveError | DiscoveryError
