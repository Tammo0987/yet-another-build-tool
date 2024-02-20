package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.ResolveError
import com.github.tammo.yabt.ResolvableProject.ResolvableProject
import com.github.tammo.yabt.ResolvedProject.ResolvedProject
import com.github.tammo.yabt.{Error, ResolvableProject, ResolvedProject}

trait VerifiedProjectResolver extends ProjectResolver {

  def projectVerifier: ProjectVerifier

  abstract override def resolveProject(
      resolvableProject: ResolvableProject
  ): Either[ResolveError, ResolvedProject] = for {
    resolvedProject <- super.resolveProject(resolvableProject)
    verifiedProject <- projectVerifier.verifyProject(resolvedProject)
  } yield verifiedProject

}
