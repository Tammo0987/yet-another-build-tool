package com.github.tammo.yabt

import com.github.tammo.yabt.cli.ConsoleCommandLineInterface
import com.github.tammo.yabt.module.{AppServiceProvider, ClasspathModuleDiscovery}

object Main:

  private val coreApplication = new CoreApplication(
    ProjectModule.projectReader,
    ProjectModule.projectResolver,
    ConsoleCommandLineInterface(_),
    ClasspathModuleDiscovery,
    AppServiceProvider
  )

  def main(args: Array[String]): Unit =
    coreApplication.readProjectAndExecuteCommand(args)
