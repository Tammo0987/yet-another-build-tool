package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.*
import com.github.tammo.yabt.extensions.MapExtensions.liftMapToEither
import com.github.tammo.yabt.extensions.SetExtensions.liftSetToEither
import com.github.tammo.yabt.ResolvedProject.*
import com.github.tammo.yabt.{Error, ResolvedProject}

import scala.annotation.tailrec

object DefaultProjectVerifier extends ProjectVerifier {

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
          .map(mr => Name(mr.toString))
          .map(name =>
            project.modules.get(name).toRight(MissingReference(name.toString))
          )
          .liftSetToEither()
      )
      .toMap
      .liftMapToEither()
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
  ): Either[ResolveError, ResolvedProject] = {

    def findCycles(
        current: ResolvedModule,
        path: Seq[String]
    ): Either[ResolveError, ResolvedProject] = {
      val references = referenceExtractor(current)
      if (references.isEmpty) {
        Right(project)
      } else {
        references
          .map(module => {
            val resolvedModule = project.modules(Name(module.toString))
            if (path.contains(resolvedModule.name.toString)) {
              Left(CyclicReference(path :+ resolvedModule.name.toString))
            } else {
              findCycles(resolvedModule, path :+ module.toString)
            }
          })
          .liftSetToEither()
          .map(_ => project)
      }
    }

    project.modules.view
      .mapValues(module => findCycles(module, Seq.empty))
      .toMap
      .liftMapToEither()
      .flatMap(_ => Right(project))
  }

}
