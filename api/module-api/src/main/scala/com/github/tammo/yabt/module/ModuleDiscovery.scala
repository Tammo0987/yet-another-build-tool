package com.github.tammo.yabt.module

trait ModuleDiscovery:

  def discoverModules: Set[Module]
