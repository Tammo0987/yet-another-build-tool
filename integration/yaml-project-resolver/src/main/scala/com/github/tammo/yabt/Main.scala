package com.github.tammo.yabt

import com.github.tammo.yabt.ResolvedProject.ResolvedProject
import com.github.tammo.yabt.project.*
import com.github.tammo.yabt.project.yaml.YamlProjectReader

object Main {

  def main(args: Array[String]): Unit = {
    val wiredProjectProvider: ProjectReader = new YamlProjectReader

    val projectResolver: ProjectResolver = new DefaultProjectResolver {
      override def projectReader: ProjectReader = wiredProjectProvider
    }

    val projectVerifier: ProjectVerifier = DefaultProjectVerifier

    val project = for {
      project <- wiredProjectProvider.readProject()
      resolvedProject <- projectResolver.resolveProject(project)
      verifiedProject <- projectVerifier.verifyProject(resolvedProject)
    } yield verifiedProject

    project match
      case Left(error) => println(error)
      case Right(project) =>
        println(
          ResolvedProjectPrinter.writeProjectToFile("build-out.yaml", project)
        )
  }
}
