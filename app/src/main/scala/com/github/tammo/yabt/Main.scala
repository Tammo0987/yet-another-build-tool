package com.github.tammo.yabt

import com.github.tammo.yabt.cli.ConsoleCommandLineInterface
import com.github.tammo.yabt.command.CommandDomain.Command

object Main:

  private val coreApplication = new CoreApplication(
    ProjectModule.projectReader,
    ProjectModule.projectResolver,
    ConsoleCommandLineInterface(Set.empty[Command[?]])
  )

  def main(args: Array[String]): Unit =
    coreApplication.readProjectAndExecuteCommand(args)
