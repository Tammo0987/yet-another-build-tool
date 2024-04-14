package com.github.tammo.yabt.project

import com.github.tammo.yabt.ResolvedProject.ResolvedProject

trait ProjectProvider:

  def project: ResolvedProject
