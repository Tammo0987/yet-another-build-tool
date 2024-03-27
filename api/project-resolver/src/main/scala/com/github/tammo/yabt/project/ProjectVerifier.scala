package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.ResolveError
import com.github.tammo.yabt.ResolvedProject.ResolvedProject

trait ProjectVerifier:

  def verifyProject(
      project: ResolvedProject
  ): Either[ResolveError, ResolvedProject]

