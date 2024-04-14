package com.github.tammo.yabt.task.jvm.tasks

import com.github.tammo.yabt.task.Task
import com.github.tammo.yabt.task.TaskDSL.task

import java.nio.file.{Files, Path}
import java.util.Comparator

object CleanTask:

  lazy val cleanTask: Task[Unit] =
    task("clean", "Cleans all generated files.") { context =>
      val targetDirectory = context.moduleDirectory.resolve("target")

      if (Files.exists(targetDirectory))
        Files
          .walk(targetDirectory)
          .sorted(Comparator.reverseOrder())
          .forEach(Files.deleteIfExists(_))
    }
