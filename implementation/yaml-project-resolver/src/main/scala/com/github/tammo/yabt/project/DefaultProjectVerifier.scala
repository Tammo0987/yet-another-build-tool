package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.*
import com.github.tammo.yabt.ResolvedProject.*
import com.github.tammo.yabt.extensions.MapExtensions.liftToEither
import com.github.tammo.yabt.extensions.SetExtensions.liftToEither
import com.github.tammo.yabt.{Error, ResolvedProject}

object DefaultProjectVerifier extends ProjectVerifier:

  override def verifyProject(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject] = for {
    checkedReferences <- verifyReferences(project)
    checkedDependencies <- verifyNoCycleInModuleDependencies(checkedReferences)
    checkedAggregates <- verifyNoCycleInModuleAggregates(checkedDependencies)
  } yield checkedAggregates

  private def verifyReferences(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject] = for {
    checkedDependencies <- verifyDependsOnReferences(project)
    checkedAggregates <- verifyAggregatesReferences(checkedDependencies)
  } yield checkedAggregates

  private def verifyDependsOnReferences(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject] =
    verifyReferences(project, _.dependsOn)

  private def verifyAggregatesReferences(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject] =
    verifyReferences(project, _.aggregates)

  private def verifyReferences(
      project: ResolvedProject,
      referenceExtractor: ResolvedModule => Set[ModuleReference]
  ): Either[ResolveError, ResolvedProject] =
    project.modules.view
      .mapValues(
        referenceExtractor(_)
          .map(Name.apply)
          .map(name =>
            project.modules.get(name).toRight(MissingReference(name))
          )
          .liftToEither()
      )
      .toMap
      .liftToEither()
      .map(_ => project)

  private def verifyNoCycleInModuleDependencies(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject] =
    verifyNoCyclesInModuleReference(project, _.dependsOn)

  private def verifyNoCycleInModuleAggregates(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject] =
    verifyNoCyclesInModuleReference(project, _.aggregates)

  private def verifyNoCyclesInModuleReference(
      project: ResolvedProject,
      referenceExtractor: ResolvedModule => Set[ModuleReference]
  ): Either[ResolveError, ResolvedProject] =

    def findCycles(
        current: ResolvedModule,
        path: Seq[String]
    ): Either[ResolveError, ResolvedProject] =
      val references = referenceExtractor(current)
      if (references.isEmpty)
        Right(project)
      else
        references
          .map(module => {
            val resolvedModule = project.modules(Name(module))
            if (path.contains(resolvedModule.name)) {
              Left(CyclicReference(path :+ resolvedModule.name))
            } else {
              findCycles(resolvedModule, path :+ module)
            }
          })
          .liftToEither()
          .map(_ => project)

    project.modules.view
      .mapValues(module => findCycles(module, Seq.empty))
      .toMap
      .liftToEither()
      .flatMap(_ => Right(project))

