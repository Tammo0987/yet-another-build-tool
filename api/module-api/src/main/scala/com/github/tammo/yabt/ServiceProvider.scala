package com.github.tammo.yabt

import com.github.tammo.yabt.dependency.DependencyResolver

trait ServiceProvider:

  def dependencyResolver: DependencyResolver
