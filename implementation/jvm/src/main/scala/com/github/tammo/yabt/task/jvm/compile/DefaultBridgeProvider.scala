package com.github.tammo.yabt.task.jvm.compile

import xsbti.Logger
import xsbti.compile.{CompilerBridgeProvider, ScalaInstance}

import java.io.File

// todo solve better
class DefaultBridgeProvider(
    private val scalaInstance: ScalaInstance,
    private val compilerBridgeJarFile: File
) extends CompilerBridgeProvider {
  override def fetchCompiledBridge(
      scalaInstance: ScalaInstance,
      logger: Logger
  ): File = compilerBridgeJarFile

  override def fetchScalaInstance(
      scalaVersion: String,
      logger: Logger
  ): ScalaInstance = scalaInstance

}
