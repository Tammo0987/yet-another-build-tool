package com.github.tammo.yabt

import com.github.tammo.yabt.cli.CommandLineInterface
import com.github.tammo.yabt.project.{ProjectReader, ProjectResolver}

class CoreApplication(
    private val projectReader: ProjectReader,
    private val projectResolver: ProjectResolver,
    private val commandLineInterface: CommandLineInterface
) {

  def readProjectAndExecuteCommand(input: Array[String]): Unit = {
    val project = for {
      project <- projectReader.readProject()
      resolvedProject <- projectResolver.resolveProject(project)
    } yield resolvedProject

    project match
      case Left(value)  => println(value)
      case Right(value) => println(value)

    println(commandLineInterface.processArguments(input))
  }

}
