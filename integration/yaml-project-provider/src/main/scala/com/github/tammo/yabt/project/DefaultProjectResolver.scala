package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.*
import com.github.tammo.yabt.extensions.MapExtensions.liftMapToEither
import com.github.tammo.yabt.ResolvableProject.{Scope as ResolvableScope, *}
import com.github.tammo.yabt.ResolvedProject.*
import com.github.tammo.yabt.extensions.SetExtensions.liftSetToEither

trait DefaultProjectResolver extends ProjectResolver {

  def projectReader: ProjectReader

  private val ROOT = "build"

  override def resolveProject(
      resolvableProject: ResolvableProject
  ): Either[ResolveError, ResolvedProject] = for {
    resolvedModules <- readModuleIncludes(
      resolvableProject.modules.values.toSet
    )
    resolvedProject <- resolveProject(resolvableProject, resolvedModules)
  } yield resolvedProject

  private def combineModuleIncludesRecursive(
      resolvableModule: Either[ResolveError, ResolvableModule],
      path: Seq[String]
  ): Either[ResolveError, ResolvableModule] = {
    for {
      module <- resolvableModule

      resolvedModules <- Right(
        module.includes.map(include =>
          combineModuleIncludesRecursive(
            checkCyclesAndResolveModuleInclude(include, path),
            path :+ include
          )
        )
      )

      combinedResolvableModule <- combineModulesIncludes(
        resolvedModules
      )
    } yield module ++ combinedResolvableModule
  }

  private def checkCyclesAndResolveModuleInclude(
      include: String,
      path: Seq[String]
  ): Either[ResolveError, ResolvableModule] = {
    if (include == ROOT) {
      Left(IllegalRootReference(path))
    } else if (path.contains(include)) {
      Left(CyclicReference(path :+ include))
    } else {
      projectReader.readModuleInclude(s"$include.yaml")
    }
  }

  private def combineModulesIncludes(
      includes: Seq[Either[ResolveError, ResolvableModule]]
  ): Either[ResolveError, ResolvableModule] = {
    includes
      .foldLeft[Either[ResolveError, ResolvableModule]](
        Right(ResolvableModule())
      )((f, s) =>
        for {
          a <- f
          b <- s
        } yield b ++ a
      )
  }

  private def readModuleIncludes(
      modules: Set[ResolvableModule]
  ): Either[ResolveError, Map[Name, ResolvedModule]] = {
    modules
      .map { module =>
        combineModuleIncludesRecursive(Right(module), Seq(ROOT))
      }
      .liftSetToEither()
      .map(set =>
        set.groupMapReduce(module => Name.apply(module.name.get))(identity) {
          case (_, s) => s
        }
      )
      .flatMap { map =>
        map.view.mapValues(resolveModules).toMap.liftMapToEither()
      }
  }

  private def resolveModules(
      module: ResolvableModule
  ): Either[ResolveError, ResolvedModule] = {
    for {
      name <- module.name.map(Name.apply).toRight(MissingField("name"))
      organization <- module.organization
        .map(Organization.apply)
        .toRight(MissingField("organization"))
      version <- module.version
        .map(Version.apply)
        .toRight(MissingField("version"))
      directory <- module.directory.toRight(MissingField("directory"))
      scalaVersion <- module.scalaVersion.toRight(
        MissingField("scala version")
      )
    } yield ResolvedModule(
      name,
      organization,
      version,
      directory,
      scalaVersion,
      module.dependencies.map(mapDependency),
      module.dependsOn.map(ModuleReference.apply),
      module.aggregates.map(ModuleReference.apply),
      module.plugins
    )
  }

  private def resolveProject(
      resolvableProject: ResolvableProject,
      modules: Map[Name, ResolvedModule]
  ): Either[ResolveError, ResolvedProject] = Right(
    ResolvedProject(
      Name(resolvableProject.name),
      Organization(resolvableProject.organization),
      Version(resolvableProject.version),
      resolvableProject.scalaVersion,
      resolvableProject.plugins,
      modules
    )
  )

  private def mapDependency(dependency: Dependency): ResolvedDependency =
    ResolvedDependency(
      Organization(dependency.organization),
      Name(dependency.name),
      Version(dependency.version),
      dependency.scope match
        case ResolvableScope.Compile => Scope.Compile
        case ResolvableScope.Test    => Scope.Test
    )

}
