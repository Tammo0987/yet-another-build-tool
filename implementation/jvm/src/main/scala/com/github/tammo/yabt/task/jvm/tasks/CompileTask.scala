package com.github.tammo.yabt.task.jvm.tasks

import com.github.tammo.yabt.dependency.DependencyDomain.*
import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.extensions.PathExtensions.*
import com.github.tammo.yabt.task.Task.Pure
import com.github.tammo.yabt.task.TaskDSL.task
import com.github.tammo.yabt.task.jvm.compile.*
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
):

  lazy val compileTask: Task[Set[Path]] =
    task("compile", "Compiles all sources of a module") { context =>
      Pure(compile(context))
    }

  private val logger = LoggerFactory.getLogger(getClass)

  private def compile(context: TaskContext): Set[Path] =
    val moduleDirectory = context.moduleDirectory
    val source =
      moduleDirectory / "src" / "main" / "scala" // TODO introduce source sets

    val classesDirectory = moduleDirectory / "target" / "classes"

    if (Files.notExists(source))
      Files.createDirectories(source)

    if (Files.notExists(classesDirectory))
      Files.createDirectories(classesDirectory)

    val sources = Files
      .walk(source)
      .filter(Files.isRegularFile(_))
      .filter(_.toString.endsWith(".scala"))
      .toList
      .asScala
      .toSet

    val scalaVersion = context.module.scalaVersion

    val dependencyChanges = makeDependencyChanges(sources)
    val compilerJarPaths = dependencyResolver
      .resolveDependencies(
        ScalaVersionUtil.scalaCompilerClasspath(scalaVersion).toSeq*
      )
      .getOrElse(
        Seq.empty
      ) // TODO fix error handling after reworked task evaluation

    val output: SingleOutput = () => classesDirectory.toFile

    val bridgeProvider =
      DefaultBridgeProvider(scalaVersion, dependencyResolver, compilerJarPaths)

    val scalaCompiler =
      ScalaCompilerFactory.createScalaCompiler(scalaVersion, bridgeProvider)

    logger.info(s"Fetching dependencies ...")

    val dependencies = context.module.dependencies.toSeq.map(dependency =>
      Dependency(
        Module(GroupId(dependency.organization), ArtifactId(dependency.name)),
        Version(dependency.version)
      )
    )
    // TODO fix error handling after task evaluation
    val dependencyPaths =
      dependencyResolver.resolveDependencies(dependencies*) match
        case Left(error) =>
          throw new RuntimeException(error.message, error.throwable)
        case Right(dependencies) =>
          dependencies

    logger.info(s"Compiling ${sources.size} classes ...")

    scalaCompiler.compile(
      sources.map(PlainVirtualFile(_)).toArray,
      (compilerJarPaths ++ dependencyPaths).map(PlainVirtualFile(_)).toArray,
      PlainVirtualFileConverter(),
      dependencyChanges,
      Array.empty,
      output,
      TestAnalysesCallback,
      CompilerReporter,
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

  // TODO fix calculation; currently recompile everything every time.
  private def makeDependencyChanges(sourceSet: Set[Path]): DependencyChanges =
    new DependencyChanges:
      override def isEmpty: Boolean = false

      override def modifiedBinaries(): Array[File] = new Array[File](0)

      override def modifiedLibraries(): Array[VirtualFileRef] =
        new Array[VirtualFileRef](0)

      override def modifiedClasses(): Array[String] =
        sourceSet.map(_.toString).toArray
