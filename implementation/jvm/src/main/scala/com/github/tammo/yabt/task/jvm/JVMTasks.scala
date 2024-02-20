package com.github.tammo.yabt.task.jvm

import com.github.tammo.yabt.dependency.DependencyDomain.{Dependency, Version}
import com.github.tammo.yabt.task.Task
import com.github.tammo.yabt.task.Task.Pure
import com.github.tammo.yabt.task.TaskDSL.task
import com.github.tammo.yabt.task.jvm.compile.{
  CompileTask,
  CoursierDependencyResolver,
  DefaultBridgeProvider,
  DefaultScalaCompiler
}

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
      val dependencyResolver = CoursierDependencyResolver

      val compilerBridgeFile = dependencyResolver
        .resolveDependencies(
          // zinc version
          Seq(Dependency(DefaultScalaCompiler.compilerBridge, Version("1.9.6")))
        )
        .head
        .toFile

      val bridgeProvider =
        DefaultBridgeProvider(null, compilerBridgeFile)
      val scalaCompiler = DefaultScalaCompiler(
        "2.13.12",
        dependencyResolver,
        bridgeProvider
      )
      val compileTask = CompileTask(dependencyResolver, scalaCompiler)

      Pure(compileTask.compile(using context))
    }

}
