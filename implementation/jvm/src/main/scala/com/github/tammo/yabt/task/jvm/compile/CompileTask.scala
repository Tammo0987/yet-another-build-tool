package com.github.tammo.yabt.task.jvm.compile

import com.github.tammo.yabt.dependency.DependencyDomain.*
import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.task.TaskContext
import com.github.tammo.yabt.task.jvm.TestAnalysesCallback
import sbt.internal.inc.{PlainVirtualFile, PlainVirtualFileConverter}
import xsbti.*
import xsbti.compile.{DependencyChanges, ScalaCompiler, SingleOutput}

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Optional
import java.util.function.Supplier
import scala.annotation.targetName
import scala.jdk.CollectionConverters.*

class CompileTask(
    private val dependencyResolver: DependencyResolver,
    private val scalaCompiler: ScalaCompiler
) {

  private val libraryModule =
    Module(GroupId("org.scala-lang"), ArtifactId("scala-library"))

  def compile(using context: TaskContext): Seq[Path] = {
    val moduleDirectory = context.workingDirectory / context.module.toString
    val source = moduleDirectory / "src" / "main" / "scala" // TODO source sets
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
          println(problem.toString)

        override def comment(pos: Position, msg: String): Unit = ()
      },
      Optional.empty(),
      new Logger {
        override def error(msg: Supplier[String]): Unit = println(msg.get())

        override def warn(msg: Supplier[String]): Unit = println(msg.get())

        override def info(msg: Supplier[String]): Unit = println(msg.get())

        override def debug(msg: Supplier[String]): Unit = println(msg.get())

        override def trace(exception: Supplier[Throwable]): Unit =
          println(exception.get().getMessage)
      }
    )
    Seq.empty
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

  // TODO place in shared module?
  extension (path: Path) {

    @targetName("slash")
    private def /(subPath: Path): Path = path.resolve(subPath)

    @targetName("slash")
    private def /(subPath: String): Path = /(Path.of(subPath))

  }

}
