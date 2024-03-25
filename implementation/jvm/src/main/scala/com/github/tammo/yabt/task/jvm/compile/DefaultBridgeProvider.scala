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
    val scalaOrganization = ScalaVersionUtil.scalaOrganization(scalaVersion)
    val bridgeDependency: Dependency = scalaVersion match
      case s"0.$_" =>
        Dependency(
          Module(GroupId(scalaOrganization), ArtifactId("dotty-sbt-bridge")),
          Version(scalaVersion)
        )
      case s"3.$_" =>
        Dependency(
          Module(GroupId(scalaOrganization), ArtifactId("scala3-sbt-bridge")),
          Version(scalaVersion)
        )
      case _ =>
        val scalaBinaryVersion =
          ScalaVersionUtil.scalaBinaryVersion(scalaVersion)
        Dependency(
          Module(
            GroupId("org.scala-sbt"),
            ArtifactId(s"compiler-bridge_$scalaBinaryVersion")
          ),
          Version(ZINC_VERSION)
        )

    dependencyResolver.resolveDependencies(Seq(bridgeDependency)).head.toFile
  }

  override def fetchScalaInstance(
      scalaVersion: String,
      logger: Logger
  ): ScalaInstance = {
    val loadedCompilerJars = fetchCompiler() // TODO lazy?
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
    dependencyResolver.resolveDependencies(
      ScalaVersionUtil.scalaCompilerClasspath(scalaVersion).toSeq
    )

  private def fetchLibrary(): Seq[Path] =
    dependencyResolver.resolveDependencies(
      ScalaVersionUtil.scalaRuntimeClasspath(scalaVersion).toSeq
    )

  private def createClassLoader(paths: Seq[Path]): ClassLoader =
    new URLClassLoader(paths.map(_.toUri.toURL).toArray, null)

object DefaultBridgeProvider:

  // TODO get from build information
  private val ZINC_VERSION = "1.9.6"
