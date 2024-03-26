package com.github.tammo.yabt.dependency

import com.github.tammo.yabt.dependency.DependencyDomain.{Dependency, DependencyResolveError}

import java.nio.file.Path

trait DependencyResolver:

  def resolveDependencies(
      dependencies: Dependency*
  ): Either[DependencyResolveError, Seq[Path]]
