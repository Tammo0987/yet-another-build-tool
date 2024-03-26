package com.github.tammo.yabt.dependency
object DependencyDomain:

  opaque type Version <: String = String
  object Version:
    def apply(version: String): Version = version

  opaque type GroupId <: String = String
  object GroupId:
    def apply(groupId: String): GroupId = groupId

  opaque type ArtifactId <: String = String
  object ArtifactId:
    def apply(artifactId: String): ArtifactId = artifactId

  case class Module(groupId: GroupId, artifactId: ArtifactId)

  case class Dependency(module: Module, version: Version):

    override def toString: String =
      s"${module.groupId}:${module.artifactId}:$version"
