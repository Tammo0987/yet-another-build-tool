package com.github.tammo.yabt.module

import com.github.tammo.yabt.ServiceProvider
import com.github.tammo.yabt.dependency.{
  CoursierDependencyResolver,
  DependencyResolver,
  LoggedDependencyResolver
}

object AppServiceProvider extends ServiceProvider:
  override def dependencyResolver: DependencyResolver =
    new CoursierDependencyResolver with LoggedDependencyResolver
