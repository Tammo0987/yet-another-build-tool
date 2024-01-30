package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.ResolveError
import com.github.tammo.yabt.ResolvableProject.ResolvableProject
import com.github.tammo.yabt.ResolvedProject.ResolvedProject

trait ProjectResolver {

  def resolveProject(
      resolvableProject: ResolvableProject
  ): Either[ResolveError, ResolvedProject]

}
