package com.github.tammo.yabt

import com.github.tammo.yabt.project.*
import com.github.tammo.yabt.project.yaml.YamlProjectReader

object ProjectModule:

  val projectReader: ProjectReader = new YamlProjectReader

  val projectResolver: ProjectResolver = new DefaultProjectResolver(
    projectReader
  ) with VerifiedProjectResolver:
    override def projectVerifier: ProjectVerifier =
      ProjectModule.projectVerifier

  private val projectVerifier: ProjectVerifier = DefaultProjectVerifier
