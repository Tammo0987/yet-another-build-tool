package com.github.tammo.yabt

import com.github.tammo.yabt.ResolvedProject.ModuleReference
import com.github.tammo.yabt.project.*
import com.github.tammo.yabt.project.yaml.YamlProjectReader
import com.github.tammo.yabt.task.jvm.JVMTasks
import com.github.tammo.yabt.task.{SequentialTaskEvaluator, TaskContext, TaskEvaluator}

import java.nio.file.Paths

object Main:

  def main(args: Array[String]): Unit = {
    val wiredProjectReader = new YamlProjectReader

    val wiredProjectVerified = DefaultProjectVerifier

    val wiredProjectResolver = new DefaultProjectResolver
      with VerifiedProjectResolver {
      override def projectVerifier: ProjectVerifier = wiredProjectVerified

      override def projectReader: ProjectReader = wiredProjectReader
    }

    val project = for {
      project <- wiredProjectReader.readProject()
      resolvedProject <- wiredProjectResolver.resolveProject(project)
    } yield resolvedProject

    project match
      case Left(value)  => println(value)
      case Right(value) => println(value)

    val wiredTaskEvaluator: TaskEvaluator = SequentialTaskEvaluator

    wiredTaskEvaluator.evaluateTask(JVMTasks.compile)(using
      TaskContext(Paths.get("").toAbsolutePath, ModuleReference("test-module"))
    )
  }
