package com.github.tammo.yabt.dependency
import com.github.tammo.yabt.dependency.DependencyDomain.{
  Dependency,
  DependencyResolveError
}
import org.slf4j.LoggerFactory

import java.nio.file.Path

trait LoggedDependencyResolver extends DependencyResolver:

  private val logger = LoggerFactory.getLogger(getClass)

  abstract override def resolveDependencies(
      dependencies: Dependency*
  ): Either[DependencyResolveError, Seq[Path]] =
    dependencies.foreach(dependency =>
      logger.debug(s"Fetching dependency $dependency")
    )
    super.resolveDependencies(dependencies*)
