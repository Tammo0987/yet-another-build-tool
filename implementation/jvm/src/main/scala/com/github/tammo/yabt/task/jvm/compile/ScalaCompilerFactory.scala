package com.github.tammo.yabt.task.jvm.compile

import sbt.internal.inc.AnalyzingCompiler
import xsbti.compile.{ClasspathOptions, CompilerBridgeProvider, ScalaCompiler}

object ScalaCompilerFactory:

  def createScalaCompiler(
      scalaVersion: String,
      bridgeProvider: CompilerBridgeProvider
  ): ScalaCompiler =
    val scalaInstance =
      bridgeProvider.fetchScalaInstance(scalaVersion, CompilerLoggerAdapter)
    new AnalyzingCompiler(
      scalaInstance = scalaInstance,
      provider = bridgeProvider,
      classpathOptions =
        ClasspathOptions.create(false, false, false, false, false),
      onArgsHandler = println,
      classLoaderCache = None
    )

