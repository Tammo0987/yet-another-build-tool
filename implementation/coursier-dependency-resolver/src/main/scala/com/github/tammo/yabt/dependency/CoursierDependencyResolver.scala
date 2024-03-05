package com.github.tammo.yabt.dependency

import coursier.core.{Dependency, Module}
import coursier.{Fetch, ModuleName, Organization}

import java.nio.file.Path

object CoursierDependencyResolver extends DependencyResolver {

  override def resolveDependencies(
      seq: Seq[DependencyDomain.Dependency]
  ): Seq[Path] = Fetch()
    .withDependencies(seq.map(toCoursierDependency))
    .run()
    .map(_.toPath)

  private def toCoursierDependency(
      dependency: DependencyDomain.Dependency
  ): Dependency = {
    Dependency(
      Module(
        Organization(dependency.module.groupId.toString),
        ModuleName(dependency.module.artifactId.toString),
        Map.empty
      ),
      dependency.version.toString
    )
  }

}
