package com.github.tammo.yabt.module

import com.github.tammo.yabt.extensions.SetExtensions.liftToEither
import com.github.tammo.yabt.module.ModuleDiscovery.DiscoveryError

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*
import scala.util.Try

object ClasspathModuleDiscovery extends ModuleDiscovery:

  private val YABT_PACKAGE = "com.github.tammo.yabt"

  override def discoverModules: Either[DiscoveryError, Set[Module]] =
    loadClassesInPackage(YABT_PACKAGE, getClass.getClassLoader)
      .flatMap:
        _.filter(classOf[Module].isAssignableFrom(_))
          .filter(x => x != classOf[Module])
          .map(loadModule)
          .liftToEither()

  private def loadClassesInPackage(
      packageName: String,
      classLoader: ClassLoader
  ): Either[DiscoveryError, Set[Class[?]]] =
    classLoader
      .getResources(packageName.replace('.', '/'))
      .asIterator()
      .asScala
      .toSet
      .filter(_.getProtocol == "file")
      .map(url => Path.of(url.toURI))
      .filter(Files.isDirectory(_))
      .map(loadClassesInDirectory(_, packageName))
      .liftToEither()
      .map(_.flatten)

  private def loadClassesInDirectory(
      directory: Path,
      packageName: String
  ): Either[DiscoveryError, Set[Class[?]]] = Files
    .walk(directory)
    .toList
    .asScala
    .filter(_.toString.endsWith(".class"))
    .map(directory.relativize)
    .map(path =>
      s"$packageName.${path.toString.replace("/", ".").dropRight(6)}"
    )
    .map(className =>
      Try(Class.forName(className)).toEither.left.map(t =>
        DiscoveryError(t.getMessage, t)
      )
    )
    .toSet
    .liftToEither()

  private def loadModule(clazz: Class[?]): Either[DiscoveryError, Module] =
    val classInstance = Try(
      clazz.getDeclaredConstructor().newInstance().asInstanceOf[Module]
    )
    val objectInstance = Try(
      clazz.getField("MODULE$").get(clazz).asInstanceOf[Module]
    )

    classInstance
      .orElse(objectInstance)
      .toEither
      .left
      .map { t =>
        DiscoveryError(t.getMessage, t)
      }
