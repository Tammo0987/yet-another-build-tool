package com.github.tammo.yabt.task

import com.github.tammo.yabt.ResolvedProject.{ModuleReference, ResolvedProject}

import java.nio.file.Path

case class TaskContext(
    workingDirectory: Path,
    rootProject: ResolvedProject,
    module: ModuleReference,
    scalaVersion: String // TODO fix this after fixing the modules and projects domain
)
