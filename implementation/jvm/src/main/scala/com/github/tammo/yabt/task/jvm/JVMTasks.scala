package com.github.tammo.yabt.task.jvm

import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.task.Task
import com.github.tammo.yabt.task.Task.Pure
import com.github.tammo.yabt.task.TaskDSL.task
import com.github.tammo.yabt.task.jvm.compile.*

import java.nio.file.{Files, Path, Paths}
import java.util.Comparator
import scala.jdk.CollectionConverters.*

object JVMTasks {

  lazy val cleanTask: Task[Unit] =
    task("clean", "Cleans all generated files.") { context =>
      val targetDirectory =
        Paths.get(context.workingDirectory.toString, "target")

      Files
        .walk(targetDirectory)
        .sorted(Comparator.reverseOrder())
        .forEach(Files.deleteIfExists(_))

      Pure(())
    }

  lazy val collectSources: Task[Seq[Path]] =
    task("sources", "Collects all sources of a module.") { context =>
      val sourceDirectory = Paths.get(
        context.workingDirectory.toString,
        context.module.toString,
        "src",
        "main",
        "scala"
      )

      Pure(
        Files
          .walk(sourceDirectory)
          .filter(Files.isRegularFile(_))
          .toList
          .asScala
          .toSeq
      )
    }

  lazy val compile: Task[Seq[Path]] =
    task("compile", "Compiles all sources of a module.") { context =>
      val scalaVersion = "2.13.12"
      val dependencyResolver: DependencyResolver =
        ??? // TODO: how to get implementations?
      val bridgeProvider =
        DefaultBridgeProvider(scalaVersion, dependencyResolver)
      val scalaCompiler =
        ScalaCompilerFactory.createScalaCompiler(scalaVersion, bridgeProvider)
      val compileTask = CompileTask(dependencyResolver, scalaCompiler)

      Pure(compileTask.compile(using context))
    }

}
