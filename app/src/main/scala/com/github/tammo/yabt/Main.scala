package com.github.tammo.yabt

import com.github.tammo.yabt.CoreApplication.CoreError
import com.github.tammo.yabt.Resolve.*
import com.github.tammo.yabt.Resolve.ReadError.*
import com.github.tammo.yabt.cli.ConsoleCommandLineInterface
import com.github.tammo.yabt.dependency.CoursierDependencyResolver
import com.github.tammo.yabt.module.ClasspathModuleDiscovery
import com.github.tammo.yabt.module.ModuleDiscovery.DiscoveryError
import com.github.tammo.yabt.task.SequentialTaskEvaluator
import org.slf4j.LoggerFactory

object Main:

  private val dependencies = CoreApplication.Dependencies(
    ProjectModule.projectReader,
    ProjectModule.projectResolver,
    ConsoleCommandLineInterface(_),
    ClasspathModuleDiscovery,
    SequentialTaskEvaluator,
    CoursierDependencyResolver
  )

  private val coreApplication = new CoreApplication(dependencies)

  private val logger = LoggerFactory.getLogger(Main.getClass)

  def main(args: Array[String]): Unit =
    logger.info("Loading project ...")
    coreApplication.main(args) match
      case Left(coreError) => logError(coreError)
      case _               => logger.info("Finished execution.")

  private def logError(error: CoreError): Unit = error match
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
    case DiscoveryError(message, throwable) =>
      logger.error(s"Module discovery error: $message", throwable)
