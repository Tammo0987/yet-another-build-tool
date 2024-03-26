package com.github.tammo.yabt.task.jvm.tasks

import com.github.tammo.yabt.task.Task
import com.github.tammo.yabt.task.Task.Pure
import com.github.tammo.yabt.task.TaskDSL.task

import java.nio.file.{Files, Paths}
import java.util.Comparator

object CleanTask:

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
