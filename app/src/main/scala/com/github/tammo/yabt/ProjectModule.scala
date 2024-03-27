package com.github.tammo.yabt

import com.github.tammo.yabt.project.*
import com.github.tammo.yabt.project.yaml.YamlProjectReader

object ProjectModule:

  val projectReader: ProjectReader = new YamlProjectReader

  val projectResolver: ProjectResolver = DefaultProjectResolver(projectReader)

  val projectVerifier: ProjectVerifier = DefaultProjectVerifier

