package com.github.tammo.yabt.module

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*

object ClasspathModuleDiscovery extends ModuleDiscovery:

  override def discoverModules: Set[Module] = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val resources = classLoader.getResource("")

    val classPath = Path.of(resources.toURI)

    Files
      .walk(classPath)
      .toList
      .asScala
      .filterNot(Files.isDirectory(_))
      .filter(_.toString.endsWith(".class"))
      .map(
        classPath
          .relativize(_)
          .toString
          .stripSuffix(".class")
          .replace("/", ".")
      )
      .map(Class.forName)
      .filter(classOf[Module].isAssignableFrom(_))
      .filter(x => x != classOf[Module])
      .flatMap { clazz =>
        try {
          Some(
            clazz.getDeclaredConstructor().newInstance().asInstanceOf[Module]
          )
        } catch
          // TODO change result type
          case e: Throwable =>
            println(e.getMessage)
            e.printStackTrace()
            None
      }
      .toSet
  }
