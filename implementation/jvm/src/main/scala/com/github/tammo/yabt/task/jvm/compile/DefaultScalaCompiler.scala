package com.github.tammo.yabt.task.jvm.compile

import com.github.tammo.yabt.dependency.DependencyDomain.*
import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.task.jvm.compile.DefaultScalaCompiler.*
import sbt.internal.inc.{AnalyzingCompiler, ScalaInstance}
import xsbti.*
import xsbti.compile.*

import java.net.URLClassLoader
import java.nio.file.Path
import java.util.Optional

// TODO just a setup function?
class DefaultScalaCompiler(
    private val scalaVersion: String,
    private val dependencyResolver: DependencyResolver,
    private val compilerBridgeProvider: CompilerBridgeProvider
) extends ScalaCompiler {

  private lazy val xscalaInstance = scalaInstance()

  private lazy val analyzingCompiler = new AnalyzingCompiler(
    xscalaInstance,
    compilerBridgeProvider,
    classpathOptions(),
    println,
    None
  )

  override def scalaInstance(): ScalaInstance = {
    val compilerJars = fetchCompiler()
    val libraryJars = fetchLibrary()

    new ScalaInstance(
      version = scalaVersion,
      loader = createClassLoader(compilerJars ++ libraryJars),
      loaderCompilerOnly = createClassLoader(compilerJars),
      loaderLibraryOnly = createClassLoader(libraryJars),
      libraryJars = libraryJars.map(_.toFile).toArray,
      compilerJars = compilerJars.map(_.toFile).toArray,
      allJars = (compilerJars ++ libraryJars).map(_.toFile).toArray,
      explicitActual = Some(scalaVersion)
    )
  }

  override def classpathOptions(): ClasspathOptions =
    ClasspathOptions.create(false, false, false, false, false)

  override def compile(
      sources: Array[VirtualFile],
      classpath: Array[VirtualFile],
      converter: FileConverter,
      changes: DependencyChanges,
      options: Array[String],
      output: Output,
      callback: AnalysisCallback,
      reporter: Reporter,
      progressOpt: Optional[CompileProgress],
      log: Logger
  ): Unit = analyzingCompiler
    .compile(
      sources,
      classpath,
      converter,
      changes,
      options,
      output,
      callback,
      reporter,
      progressOpt,
      log
    )

  private def fetchCompiler(): Seq[Path] = fetchModule(compilerModule)

  private def fetchLibrary(): Seq[Path] = fetchModule(libraryModule)

  private def fetchModule(module: Module): Seq[Path] =
    dependencyResolver.resolveDependencies(
      Seq(Dependency(module, Version(scalaVersion)))
    )

  private def createClassLoader(paths: Seq[Path]): ClassLoader =
    new URLClassLoader(paths.map(_.toUri.toURL).toArray, null)

}

object DefaultScalaCompiler:

  lazy val compilerBridge: Module = Module(
    GroupId("org.scala-sbt"),
    ArtifactId("compiler-bridge_2.13")
  )

  private val compilerModule =
    Module(GroupId("org.scala-lang"), ArtifactId("scala-compiler"))

  private val libraryModule =
    Module(GroupId("org.scala-lang"), ArtifactId("scala-library"))
