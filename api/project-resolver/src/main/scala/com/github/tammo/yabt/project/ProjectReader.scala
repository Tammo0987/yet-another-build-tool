package com.github.tammo.yabt.project

import com.github.tammo.yabt.Error.ResolveError
import com.github.tammo.yabt.ResolvableProject.{
  ResolvableModule,
  ResolvableProject
}

trait ProjectReader:

  def readProject(): Either[ResolveError, ResolvableProject]

  def readModuleInclude(include: String): Either[ResolveError, ResolvableModule]

