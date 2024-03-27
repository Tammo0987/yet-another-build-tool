package com.github.tammo.yabt.task

import com.github.tammo.yabt.ResolvedProject.Module.*
import com.github.tammo.yabt.ResolvedProject.{Module, ResolvedProject}

import java.nio.file.Path

case class TaskContext(
    workingDirectory: Path,
    rootProject: ResolvedProject,
    module: Module
):

  def moduleDirectory: Path =
    module match
      case module: ResolvedModule =>
        workingDirectory.resolve(module.directory)
      case _: RootModule =>
        workingDirectory
