package com.github.tammo.yabt

import com.github.tammo.yabt.ResolvedProject.Module.RootModule

object ResolvedProject:

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

  case class ResolvedProject(
      name: Name,
      organization: Organization,
      version: Version,
      scalaVersion: String,
      plugins: Set[String],
      dependencies: Set[ResolvedDependency],
      dependsOn: Set[ModuleReference],
      aggregates: Seq[ModuleReference],
      modules: Map[ModuleReference, Module.ResolvedModule]
  ):

    def toModule: RootModule = RootModule(
      organization,
      version,
      scalaVersion,
      plugins,
      dependencies,
      dependsOn,
      aggregates
    )

  enum Module(
      val organization: Organization,
      val version: Version,
      val scalaVersion: String,
      val plugins: Set[String],
      val dependencies: Set[ResolvedDependency],
      val dependsOn: Set[ModuleReference],
      val aggregates: Seq[ModuleReference]
  ):

    case ResolvedModule(
        name: ModuleReference,
        override val organization: Organization,
        override val version: Version,
        directory: String,
        override val scalaVersion: String,
        override val plugins: Set[String],
        override val dependencies: Set[ResolvedDependency],
        override val dependsOn: Set[ModuleReference],
        override val aggregates: Seq[ModuleReference]
    ) extends Module(
          organization,
          version,
          scalaVersion,
          plugins,
          dependencies,
          dependsOn,
          aggregates
        )

    case RootModule(
        override val organization: Organization,
        override val version: Version,
        override val scalaVersion: String,
        override val plugins: Set[String],
        override val dependencies: Set[ResolvedDependency],
        override val dependsOn: Set[ModuleReference],
        override val aggregates: Seq[ModuleReference]
    ) extends Module(
          organization,
          version,
          scalaVersion,
          plugins,
          dependencies,
          dependsOn,
          aggregates
        )

  case class ResolvedDependency(
      organization: Organization,
      name: Name,
      version: Version,
      scope: Scope
  )

  enum Scope:
    case Compile
    case Test
