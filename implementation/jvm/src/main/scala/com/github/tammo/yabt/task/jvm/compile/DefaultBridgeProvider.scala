package com.github.tammo.yabt.task.jvm.compile

import com.github.tammo.yabt.dependency.DependencyDomain.*
import com.github.tammo.yabt.dependency.DependencyResolver
import com.github.tammo.yabt.task.jvm.compile.DefaultBridgeProvider.*
import xsbti.Logger
import xsbti.compile.{CompilerBridgeProvider, ScalaInstance}

import java.io.File
import java.net.URLClassLoader
import java.nio.file.Path

class DefaultBridgeProvider(
    private val scalaVersion: String,
    private val dependencyResolver: DependencyResolver
) extends CompilerBridgeProvider:

  override def fetchCompiledBridge(
      scalaInstance: ScalaInstance,
      logger: Logger
  ): File = {
    val bridgeJar = fetchModule(compilerBridge, Version("1.9.6")).head.toFile

    bridgeJar
  }

  override def fetchScalaInstance(
      scalaVersion: String,
      logger: Logger
  ): ScalaInstance = {
    val loadedCompilerJars = fetchCompiler()
    val loadedLibraryJars = fetchLibrary()

    new ScalaInstance:
      // todo binary version?
      override def version(): String = scalaVersion

      override def loader(): ClassLoader = createClassLoader(
        loadedCompilerJars ++ loadedLibraryJars
      )

      override def loaderCompilerOnly(): ClassLoader = createClassLoader(
        loadedCompilerJars
      )

      override def loaderLibraryOnly(): ClassLoader = createClassLoader(
        loadedLibraryJars
      )

      override def libraryJars(): Array[File] =
        loadedLibraryJars.map(_.toFile).toArray

      override def compilerJars(): Array[File] =
        loadedCompilerJars.map(_.toFile).toArray

      override def otherJars(): Array[File] =
        allJars().filterNot(jar =>
          compilerJars().contains(jar) || libraryJars().contains(jar)
        )

      override def allJars(): Array[File] = compilerJars ++ libraryJars

      override def actualVersion(): String = scalaVersion
  }

  private def fetchCompiler(): Seq[Path] =
    fetchModule(compilerModule, Version(scalaVersion))

  private def fetchLibrary(): Seq[Path] =
    fetchModule(libraryModule, Version(scalaVersion))

  private def fetchModule(module: Module, version: Version): Seq[Path] =
    dependencyResolver.resolveDependencies(
      Seq(Dependency(module, version))
    )

  private def createClassLoader(paths: Seq[Path]): ClassLoader =
    new URLClassLoader(paths.map(_.toUri.toURL).toArray, null)

object DefaultBridgeProvider:

  lazy val compilerBridge: Module = Module(
    GroupId("org.scala-sbt"),
    ArtifactId("compiler-bridge_2.13") // TODO version
  )

  private val compilerModule =
    Module(GroupId("org.scala-lang"), ArtifactId("scala-compiler"))

  private val libraryModule =
    Module(GroupId("org.scala-lang"), ArtifactId("scala-library"))
