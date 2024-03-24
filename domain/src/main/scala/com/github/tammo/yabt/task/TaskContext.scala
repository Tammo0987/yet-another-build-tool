package com.github.tammo.yabt.task

import com.github.tammo.yabt.ResolvedProject.{ModuleReference, ResolvedProject}

import java.nio.file.Path

case class TaskContext(
    workingDirectory: Path,
    rootProject: ResolvedProject,
    module: ModuleReference
)
