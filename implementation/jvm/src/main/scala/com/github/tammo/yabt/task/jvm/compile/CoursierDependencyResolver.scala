package com.github.tammo.yabt.task.jvm.compile

import com.github.tammo.yabt.dependency.{DependencyDomain, DependencyResolver}
import coursier.core.{Dependency, Module}
import coursier.{Fetch, ModuleName, Organization}

import java.nio.file.Path

// TODO move to another module
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
