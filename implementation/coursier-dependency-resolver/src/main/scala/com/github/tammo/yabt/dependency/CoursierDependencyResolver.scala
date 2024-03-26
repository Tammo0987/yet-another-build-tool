package com.github.tammo.yabt.dependency

import com.github.tammo.yabt.dependency.DependencyDomain.DependencyResolveError
import coursier.core.{Dependency, Module}
import coursier.{Fetch, ModuleName, Organization}

import java.nio.file.Path

class CoursierDependencyResolver extends DependencyResolver:

  override def resolveDependencies(
      dependencies: DependencyDomain.Dependency*
  ): Either[DependencyResolveError, Seq[Path]] =
    Fetch()
      .withDependencies(dependencies.map(toCoursierDependency))
      .either()
      .map(_.map(_.toPath))
      .left
      .map(error => DependencyResolveError(error.getMessage, error.getCause))

  private def toCoursierDependency(
      dependency: DependencyDomain.Dependency
  ): Dependency = Dependency(
    Module(
      Organization(dependency.module.groupId),
      ModuleName(dependency.module.artifactId),
      Map.empty
    ),
    dependency.version
  )
