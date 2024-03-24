package com.github.tammo.yabt.task.jvm.tasks

import com.github.tammo.yabt.dependency.DependencyDomain.*
import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.extensions.PathExtensions.*
import com.github.tammo.yabt.task.Task.Pure
import com.github.tammo.yabt.task.TaskDSL.task
import com.github.tammo.yabt.task.jvm.TestAnalysesCallback
import com.github.tammo.yabt.task.jvm.compile.{
  CompilerLoggerAdapter,
  DefaultBridgeProvider,
  ScalaCompilerFactory
}
import com.github.tammo.yabt.task.jvm.tasks.CompileTask.*
import com.github.tammo.yabt.task.{Task, TaskContext}
import org.slf4j.LoggerFactory
import sbt.internal.inc.{PlainVirtualFile, PlainVirtualFileConverter}
import xsbti.*
import xsbti.compile.*

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Optional
import scala.jdk.CollectionConverters.*

class CompileTask(
    private val dependencyResolver: DependencyResolver
) {

  lazy val compileTask: Task[Set[Path]] =
    task("compile", "Compiles all sources of a module") { context =>
      Pure(compile(context))
    }

  private val logger = LoggerFactory.getLogger(getClass)

  private def compile(context: TaskContext): Set[Path] = {
    val moduleDirectory =
      context.workingDirectory /// context.module.toString // TODO fix module access
    val source =
      moduleDirectory / "src" / "main" / "scala" // TODO introduce source sets

    val classesDirectory = moduleDirectory / "target" / "classes"

    if (Files.notExists(source)) {
      Files.createDirectories(source)
    }

    if (Files.notExists(classesDirectory)) {
      Files.createDirectories(classesDirectory)
    }

    val sources = Files
      .walk(source)
      .filter(Files.isRegularFile(_))
      .filter(_.toString.endsWith(".scala"))
      .toList
      .asScala
      .toSet

    val dependencyChanges = makeDependencyChanges(sources)
    val library = dependencyResolver.resolveDependencies(
      Seq(Dependency(libraryModule, Version("2.13.10")))
    )

    val output: SingleOutput = () => classesDirectory.toFile

    // TODO probably calculate from module before executing task
    val scalaVersion = context.rootProject.scalaVersion

    val bridgeProvider =
      DefaultBridgeProvider(scalaVersion, dependencyResolver)

    val scalaCompiler =
      ScalaCompilerFactory.createScalaCompiler(scalaVersion, bridgeProvider)

    logger.info(s"Compiling ${sources.size} classes ...")

    scalaCompiler.compile(
      sources.map(PlainVirtualFile(_)).toArray,
      library.map(PlainVirtualFile(_)).toArray,
      PlainVirtualFileConverter(),
      dependencyChanges,
      Array.empty,
      output,
      TestAnalysesCallback,
      new Reporter {
        override def reset(): Unit = ()

        override def hasErrors: Boolean = false

        override def hasWarnings: Boolean = false

        override def printSummary(): Unit = ()

        override def problems(): Array[Problem] = Array.empty

        override def log(problem: Problem): Unit =
          logger.info(s"P: ${problem.toString}")

        override def comment(pos: Position, msg: String): Unit = ()
      },
      Optional.empty(),
      CompilerLoggerAdapter
    )

    logger.info("Compilation done.")

    Files
      .walk(classesDirectory)
      .filter(_.endsWith(".class"))
      .toList
      .asScala
      .toSet
  }

  // TODO fix calculation
  private def makeDependencyChanges(sourceSet: Set[Path]): DependencyChanges =
    new DependencyChanges:
      override def isEmpty: Boolean = false

      override def modifiedBinaries(): Array[File] = new Array[File](0)

      override def modifiedLibraries(): Array[VirtualFileRef] =
        new Array[VirtualFileRef](0)

      override def modifiedClasses(): Array[String] =
        sourceSet.map(_.toString).toArray

}

object CompileTask:

  private lazy val libraryModule =
    Module(GroupId("org.scala-lang"), ArtifactId("scala-library"))