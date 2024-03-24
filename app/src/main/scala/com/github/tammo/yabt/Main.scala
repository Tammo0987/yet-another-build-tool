package com.github.tammo.yabt

import com.github.tammo.yabt.cli.ConsoleCommandLineInterface
import com.github.tammo.yabt.module.{
  AppServiceProvider,
  ClasspathModuleDiscovery
}
import com.github.tammo.yabt.task.SequentialTaskEvaluator
import org.slf4j.LoggerFactory

object Main:

  private val coreApplication = new CoreApplication(
    ProjectModule.projectReader,
    ProjectModule.projectResolver,
    ConsoleCommandLineInterface(_),
    ClasspathModuleDiscovery,
    AppServiceProvider,
    SequentialTaskEvaluator
  )

  private val logger = LoggerFactory.getLogger(Main.getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Loading project ...")
    coreApplication.readProjectAndExecuteCommand(args)
  }
