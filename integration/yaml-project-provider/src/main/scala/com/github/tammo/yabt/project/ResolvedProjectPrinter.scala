package com.github.tammo.yabt.project

import com.github.tammo.yabt.ResolvedProject.*
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.*
import io.circe.yaml.Printer
import io.circe.yaml.Printer.StringStyle
import io.circe.{Encoder, Json, KeyEncoder}

import java.io.FileWriter
import java.nio.file.Paths
import scala.util.Try

object ResolvedProjectPrinter {

  private given Encoder[Name] = Encoder.encodeString.contramap(_.toString)

  private given Encoder[Organization] =
    Encoder.encodeString.contramap(_.toString)

  private given Encoder[Version] = Encoder.encodeString.contramap(_.toString)

  private given Encoder[ModuleReference] =
    Encoder.encodeString.contramap(_.toString)

  private given Encoder[ResolvedDependency] = deriveEncoder[ResolvedDependency]

  private given Encoder[Scope] = (a: Scope) => Json.fromString(a.toString)

  private given Encoder[ResolvedModule] = deriveEncoder[ResolvedModule]

  private given KeyEncoder[Name] =
    KeyEncoder.encodeKeyString.contramap(_.toString)

  private given Encoder[ResolvedProject] = deriveEncoder[ResolvedProject]

  private val yamlPrinter =
    Printer(preserveOrder = true, stringStyle = StringStyle.DoubleQuoted)

  def writeProjectToFile(path: String, project: ResolvedProject): String = {
    val fw = FileWriter(path)

    Try {
      fw.write(yamlPrinter.pretty(project.asJson))
      fw.close()
    }.fold(
      e => e.getMessage,
      _ => s"${Paths.get(path).getFileName.toString} has been written"
    )
  }

}
