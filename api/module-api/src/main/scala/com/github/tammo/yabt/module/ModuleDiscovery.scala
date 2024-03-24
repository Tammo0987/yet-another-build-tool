package com.github.tammo.yabt.module

import com.github.tammo.yabt.module.ModuleDiscovery.DiscoveryError

trait ModuleDiscovery:

  def discoverModules: Either[DiscoveryError, Set[Module]]

object ModuleDiscovery:

  case class DiscoveryError(message: String, throwable: Throwable)
