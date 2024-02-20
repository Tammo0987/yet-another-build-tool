package com.github.tammo.yabt.task.jvm

import xsbti.api.{ClassLike, DependencyContext}
import xsbti.*

import java.io.File
import java.nio.file.Path
import java.util
import java.util.Optional

object TestAnalysesCallback extends AnalysisCallback2 {

  override def startSource(source: File): Unit = ()

  override def startSource(source: VirtualFile): Unit = ()

  override def classDependency(
      onClassName: String,
      sourceClassName: String,
      context: DependencyContext
  ): Unit = ()

  override def binaryDependency(
      onBinaryEntry: File,
      onBinaryClassName: String,
      fromClassName: String,
      fromSourceFile: File,
      context: DependencyContext
  ): Unit = ()

  override def binaryDependency(
      onBinaryEntry: Path,
      onBinaryClassName: String,
      fromClassName: String,
      fromSourceFile: VirtualFileRef,
      context: DependencyContext
  ): Unit = ()

  override def generatedNonLocalClass(
      source: File,
      classFile: File,
      binaryClassName: String,
      srcClassName: String
  ): Unit =
    println(srcClassName)

  override def generatedNonLocalClass(
      source: VirtualFileRef,
      classFile: Path,
      binaryClassName: String,
      srcClassName: String
  ): Unit =
    println(srcClassName)

  override def generatedLocalClass(source: File, classFile: File): Unit =
    println(source.getName)

  override def generatedLocalClass(
      source: VirtualFileRef,
      classFile: Path
  ): Unit =
    println(source.name())

  override def api(sourceFile: File, classApi: ClassLike): Unit = ()

  override def api(sourceFile: VirtualFileRef, classApi: ClassLike): Unit = ()

  override def mainClass(sourceFile: File, className: String): Unit = ()

  override def mainClass(sourceFile: VirtualFileRef, className: String): Unit =
    ()

  override def usedName(
      className: String,
      name: String,
      useScopes: util.EnumSet[UseScope]
  ): Unit = ()

  override def problem(
      what: String,
      pos: Position,
      msg: String,
      severity: Severity,
      reported: Boolean
  ): Unit =
    println(s"Problem: $msg")

  override def dependencyPhaseCompleted(): Unit = ()

  override def apiPhaseCompleted(): Unit = ()

  override def enabled(): Boolean = true

  override def classesInOutputJar(): util.Set[String] = java.util.Set.of()

  override def isPickleJava: Boolean = false

  override def getPickleJarPair: Optional[T2[Path, Path]] = Optional.empty()

  override def problem2(
      what: String,
      pos: Position,
      msg: String,
      severity: Severity,
      reported: Boolean,
      rendered: Optional[String],
      diagnosticCode: Optional[DiagnosticCode],
      diagnosticRelatedInformation: util.List[DiagnosticRelatedInformation],
      actions: util.List[Action]
  ): Unit = ()
}
