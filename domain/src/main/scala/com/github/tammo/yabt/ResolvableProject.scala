package com.github.tammo.yabt

import scala.annotation.targetName

object ResolvableProject:

  // TODO types?
  case class ResolvableProject(
      reference: String = "1",
      name: String,
      version: String,
      organization: String,
      scalaVersion: String,
      plugins: Set[String] = Set.empty,
      dependsOn: Set[String] = Set.empty,
      aggregates: Seq[String] = Seq.empty,
      dependencies: Set[Dependency] = Set.empty,
      modules: Set[ResolvableModule] = Set.empty
  )

  case class ResolvableModule(
      name: Option[String] = None,
      version: Option[String] = None,
      organization: Option[String] = None,
      directory: Option[String] = None,
      scalaVersion: Option[String] = None,
      dependencies: Set[Dependency] = Set.empty,
      dependsOn: Set[String] = Set.empty,
      aggregates: Seq[String] = Seq.empty,
      plugins: Set[String] = Set.empty,
      includes: Seq[String] = Seq.empty
  ):

    @targetName("combine")
    def ++(other: ResolvableModule): ResolvableModule =
      ResolvableModule(
        name.orElse(other.name),
        version.orElse(other.version),
        organization.orElse(other.organization),
        directory.orElse(other.directory),
        scalaVersion.orElse(other.scalaVersion),
        dependencies ++ other.dependencies,
        dependsOn ++ other.dependsOn,
        aggregates ++ other.aggregates,
        plugins ++ other.plugins,
        includes ++ other.includes
      )

  case class Dependency(
      organization: String,
      name: String,
      version: String,
      scope: Scope = Scope.Compile
  )

  enum Scope:
    case Compile
    case Test
