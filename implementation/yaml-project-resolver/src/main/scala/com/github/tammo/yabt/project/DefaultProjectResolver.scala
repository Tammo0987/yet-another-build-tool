package com.github.tammo.yabt.project

import com.github.tammo.yabt.Resolve.*
import com.github.tammo.yabt.ResolvableProject.{Scope as ResolvableScope, *}
import com.github.tammo.yabt.ResolvedProject.*
import com.github.tammo.yabt.ResolvedProject.Module.ResolvedModule
import com.github.tammo.yabt.extensions.MapExtensions.liftToEither
import com.github.tammo.yabt.extensions.SetExtensions.liftToEither

class DefaultProjectResolver(private val projectReader: ProjectReader)
    extends ProjectResolver:

  private val ROOT = "build"

  override def resolveProject(
      resolvableProject: ResolvableProject
  ): Either[ResolveError, ResolvedProject] = for {
    modules <- readModuleIncludes(
      resolvableProject.modules,
      resolvableProject
    )
    resolvedProject <- resolveProject(resolvableProject, modules)
  } yield resolvedProject

  private def combineModuleIncludesRecursive(
      resolvableModule: Either[ResolveError, ResolvableModule],
      path: Seq[String]
  ): Either[ResolveError, ResolvableModule] =
    for {
      module <- resolvableModule

      Modules <- Right(
        module.includes.map(include =>
          combineModuleIncludesRecursive(
            checkCyclesAndResolveModuleInclude(include, path),
            path :+ include
          )
        )
      )

      combinedResolvableModule <- combineModulesIncludes(
        Modules
      )
    } yield module ++ combinedResolvableModule

  private def checkCyclesAndResolveModuleInclude(
      include: String,
      path: Seq[String]
  ): Either[ResolveError, ResolvableModule] =
    if (include == ROOT)
      Left(IllegalRootReference(path))
    else if (path.contains(include))
      Left(CyclicReference(path :+ include))
    else
      projectReader.readModuleInclude(s"$include.yaml")

  private def combineModulesIncludes(
      includes: Seq[Either[ResolveError, ResolvableModule]]
  ): Either[ResolveError, ResolvableModule] =
    includes
      .foldLeft[Either[ResolveError, ResolvableModule]](
        Right(ResolvableModule())
      )((f, s) =>
        for {
          a <- f
          b <- s
        } yield b ++ a
      )

  private def readModuleIncludes(
      modules: Set[ResolvableModule],
      project: ResolvableProject
  ): Either[ResolveError, Map[ModuleReference, ResolvedModule]] =
    modules
      .map { module =>
        combineModuleIncludesRecursive(Right(module), Seq(ROOT))
      }
      .liftToEither()
      .map(set =>
        set.groupMapReduce(module => ModuleReference.apply(module.name.get))(
          identity
        ) { case (_, s) =>
          s
        }
      )
      .flatMap { map =>
        map.view.mapValues(resolveModules(_, project)).toMap.liftToEither()
      }

  private def resolveModules(
      module: ResolvableModule,
      project: ResolvableProject
  ): Either[ResolveError, ResolvedModule] =
    for {
      moduleName <- module.name
        .map(ModuleReference.apply)
        .toRight(MissingField("name"))
      organization <- module.organization
        .orElse(Some(project.organization))
        .map(Organization.apply)
        .toRight(MissingField("organization"))
      version <- module.version
        .orElse(Some(project.version))
        .map(Version.apply)
        .toRight(MissingField("version"))
      directory <- module.directory.toRight(MissingField("directory"))
      scalaVersion <- module.scalaVersion
        .orElse(Some(project.scalaVersion))
        .toRight(
          MissingField("scala version")
        )
    } yield ResolvedModule(
      moduleName,
      organization,
      version,
      directory,
      scalaVersion,
      module.plugins,
      module.dependencies.map(mapDependency),
      module.dependsOn.map(ModuleReference.apply),
      module.aggregates.map(ModuleReference.apply)
    )

  private def resolveProject(
      resolvableProject: ResolvableProject,
      modules: Map[ModuleReference, ResolvedModule]
  ): Either[ResolveError, ResolvedProject] = Right(
    ResolvedProject(
      Name(resolvableProject.name),
      Organization(resolvableProject.organization),
      Version(resolvableProject.version),
      resolvableProject.scalaVersion,
      resolvableProject.plugins,
      resolvableProject.dependencies.map(mapDependency),
      resolvableProject.dependsOn.map(ModuleReference.apply),
      resolvableProject.aggregates.map(ModuleReference.apply),
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
