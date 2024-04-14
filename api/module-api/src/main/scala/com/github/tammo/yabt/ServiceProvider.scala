package com.github.tammo.yabt

import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.project.ProjectProvider
import com.github.tammo.yabt.task.TaskEvaluator

trait ServiceProvider:

  def dependencyResolver: DependencyResolver

  def projectProvider: ProjectProvider

  def taskEvaluator: TaskEvaluator
