package com.github.tammo.yabt.task.jvm.compile

import com.github.tammo.yabt.dependency.DependencyDomain.*

object ScalaVersionUtil:

  def scalaCompilerClasspath(scalaVersion: String): Set[Dependency] = {
    val groupId = GroupId(scalaOrganization(scalaVersion))
    val version = Version(scalaVersion)
    scalaVersion match
      case s"0.$_" =>
        Set(Dependency(Module(groupId, ArtifactId("dotty-compiler")), version))
      case s"3.$_" =>
        Set(
          Dependency(Module(groupId, ArtifactId("scala3-compiler_3")), version)
        ) // TODO _3 here?
      case _ =>
        Set(
          Dependency(Module(groupId, ArtifactId("scala-compiler")), version),
          Dependency(Module(groupId, ArtifactId("scala-reflect")), version)
        )
  }

  def scalaRuntimeClasspath(scalaVersion: String): Set[Dependency] = {
    val groupId = GroupId(scalaOrganization(scalaVersion))
    val version = Version(scalaVersion)
    scalaVersion match
      case s"0.$_" =>
        Set(Dependency(Module(groupId, ArtifactId("dotty-library")), version))
      case s"3.$_" =>
        Set(
          Dependency(Module(groupId, ArtifactId("scala3-library_3")), version)
        )
      case _ =>
        Set(Dependency(Module(groupId, ArtifactId("scala-library")), version))
  }

  def scalaBinaryVersion(scalaVersion: String): String = scalaVersion match
    case s"0.$minor.$_" => s"0.$minor"
    case s"3.$_"        => "3"
    case s"2.$minor.$_" => s"2.$minor"
    case _              => scalaVersion

  def scalaOrganization(scalaVersion: String): String =
    scalaVersion match
      case s"0.$_" => "ch.epfl.lamp"
      case _       => "org.scala-lang"
