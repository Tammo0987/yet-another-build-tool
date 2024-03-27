package com.github.tammo.yabt.project.yaml

import com.github.tammo.yabt.Error.ReadError.*
import com.github.tammo.yabt.Error.ResolveError
import com.github.tammo.yabt.ResolvableProject
import com.github.tammo.yabt.ResolvableProject.{
  ResolvableModule,
  ResolvableProject
}
import com.github.tammo.yabt.project.ProjectReader
import com.github.tammo.yabt.project.yaml.ResolvableProjectDecoder.given
import io.circe.{Decoder, Json, yaml}

import java.io.FileReader
import scala.util.Try

class YamlProjectReader extends ProjectReader:

  private val buildFilePath = "build.yaml"

  override def readProject(): Either[ResolveError, ResolvableProject] =
    readYamlFile(buildFilePath)

  override def readModuleInclude(
      include: String
  ): Either[ResolveError, ResolvableModule] =
    readYamlFile(include)

  private def readYamlFile[T: Decoder](
      location: String
  ): Either[ResolveError, T] =
    val readFile = Try {
      new FileReader(location)
    }.toEither.left.map(throwable =>
      FileError(
        Option(throwable).map(_.getMessage).getOrElse("No explicit message"),
        throwable
      )
    )

    readFile
      .flatMap(parseYaml)
      .flatMap(decodeYaml[T])

  private def parseYaml(fileReader: FileReader): Either[ResolveError, Json] =
    yaml.parser
      .parse(fileReader)
      .left
      .map(parsingFailure =>
        ParseError(parsingFailure.message, parsingFailure.underlying)
      )

  private def decodeYaml[T: Decoder](yaml: Json): Either[ResolveError, T] =
    yaml
      .as[T]
      .left
      .map(error => DecodingError(error.message, error.pathToRootString))

