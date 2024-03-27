package com.github.tammo.yabt.project.yaml

import com.github.tammo.yabt.ResolvableProject.*
import io.circe.*
import io.circe.Decoder.Result
import io.circe.derivation.{Configuration, ConfiguredDecoder}

import scala.util.Try

object ResolvableProjectDecoder:

  given Configuration = Configuration.default.withDefaults

  given Decoder[ResolvableProject] =
    Decoder.derivedConfigured[ResolvableProject]

  given Decoder[ResolvableModule] = Decoder.derivedConfigured[ResolvableModule]

  given Decoder[Dependency] = Decoder.derivedConfigured[Dependency]

  given Decoder[Scope] = Decoder.decodeString.flatMap { value => _ =>
    val scope = value.toLowerCase.capitalize

    Try(Scope.valueOf(scope)).toEither.left.map { _ =>
      DecodingFailure(
        s"$scope is not a valid scope.",
        List(CursorOp.Field(value))
      )
    }
  }

