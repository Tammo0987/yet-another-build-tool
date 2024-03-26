package com.github.tammo.yabt.dependency

import coursier.core.{Dependency, Module}
import coursier.{Fetch, ModuleName, Organization}
import org.slf4j.LoggerFactory

import java.nio.file.Path

object CoursierDependencyResolver extends DependencyResolver {

  private val logger = LoggerFactory.getLogger(getClass)

  override def resolveDependencies(
      seq: Seq[DependencyDomain.Dependency]
  ): Seq[Path] = {
    seq.foreach(dependency => logger.debug(s"Fetching dependency $dependency"))
    Fetch()
      .withDependencies(seq.map(toCoursierDependency))
      .run()
      .map(_.toPath)
  }

  private def toCoursierDependency(
      dependency: DependencyDomain.Dependency
  ): Dependency = {
    Dependency(
      Module(
        Organization(dependency.module.groupId),
        ModuleName(dependency.module.artifactId),
        Map.empty
      ),
      dependency.version
    )
  }

}
