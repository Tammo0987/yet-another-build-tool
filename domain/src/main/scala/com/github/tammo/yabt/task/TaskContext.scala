package com.github.tammo.yabt.task

import com.github.tammo.yabt.ResolvedProject.{Module, ResolvedProject}

import java.nio.file.Path

case class TaskContext(
    workingDirectory: Path, // TODO derive from module?
    rootProject: ResolvedProject,
    module: Module
)
