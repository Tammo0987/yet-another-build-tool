package com.github.tammo.yabt

object ResolvedProject {

  opaque type Name <: String = String
  object Name:
    def apply(string: String): Name = string

  opaque type Version <: String = String
  object Version:
    def apply(string: String): Version = string

  opaque type Organization <: String = String
  object Organization:
    def apply(string: String): Organization = string

  opaque type ModuleReference <: String = String
  object ModuleReference:
    def apply(string: String): ModuleReference = string

  // TODO same as module?
  case class ResolvedProject(
      name: Name,
      organization: Organization,
      version: Version,
      scalaVersion: String,
      plugins: Set[String],
      modules: Map[Name, ResolvedModule]
  )

  case class ResolvedModule(
      name: Name,
      organization: Organization,
      version: Version,
      directory: String,
      scalaVersion: String,
      dependencies: Set[ResolvedDependency],
      dependsOn: Set[ModuleReference],
      aggregates: Set[ModuleReference],
      plugins: Set[String]
  )

  case class ResolvedDependency(
      organization: Organization,
      name: Name,
      version: Version,
      scope: Scope
  )

  enum Scope {
    case Compile
    case Test
  }

}
