package com.github.tammo.yabt.module

import com.github.tammo.yabt.ServiceProvider
import com.github.tammo.yabt.command.CommandDomain.Command
import com.github.tammo.yabt.task.Task

trait Module:

  def tasks: Set[Task[?]] = Set.empty

  def tasks(serviceProvider: ServiceProvider): Set[Task[?]] = Set.empty

  def commands: Set[Command[?]] = Set.empty
  def commands(serviceProvider: ServiceProvider): Set[Command[?]] = Set.empty
