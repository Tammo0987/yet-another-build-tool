package com.github.tammo.yabt.dependency

import com.github.tammo.yabt.dependency.DependencyDomain.Dependency

import java.nio.file.Path

trait DependencyResolver:

  def resolveDependencies(seq: Seq[Dependency]): Seq[Path]
