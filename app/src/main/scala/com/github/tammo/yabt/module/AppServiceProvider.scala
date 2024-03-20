package com.github.tammo.yabt.module

import com.github.tammo.yabt.ServiceProvider
import com.github.tammo.yabt.dependency.{
  CoursierDependencyResolver,
  DependencyResolver
}

object AppServiceProvider extends ServiceProvider:
  override def dependencyResolver: DependencyResolver =
    CoursierDependencyResolver
