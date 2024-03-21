package com.github.tammo.yabt

import com.github.tammo.yabt.cli.CommandLineInterface
import com.github.tammo.yabt.command.CommandDomain.Command
import com.github.tammo.yabt.module.ModuleDiscovery
import com.github.tammo.yabt.project.{ProjectReader, ProjectResolver}

class CoreApplication(
    private val projectReader: ProjectReader,
    private val projectResolver: ProjectResolver,
    private val commandLineInterfaceProvider: Set[
      Command[?]
    ] => CommandLineInterface,
    private val moduleDiscovery: ModuleDiscovery,
    private val serviceProvider: ServiceProvider
) {

  def readProjectAndExecuteCommand(input: Array[String]): Unit = {
    val project = for {
      project <- projectReader.readProject()
      resolvedProject <- projectResolver.resolveProject(project)
    } yield resolvedProject

    project match
      case Left(value)  => println(value)
      case Right(value) => println(value)

    moduleDiscovery.discoverModules match
      case Left(discoveryError) => println(discoveryError)
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
          .foreach(taskName => println(s"Recognized task: $taskName"))

        println(commandLineInterface.processArguments(input))
  }

}
