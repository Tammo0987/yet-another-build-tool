package com.github.tammo.yabt.extensions

import com.github.tammo.yabt.extensions.MapExtensions.liftMapToEither
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class MapExtensionsTest extends AnyFlatSpecLike with Matchers {

  it should "lift empty map to right of empty map" in {
    val map: Map[Int, Either[String, Int]] = Map.empty

    map.liftMapToEither() shouldBe Right(Map.empty)
  }

  it should "lift map with only right values to map with the same values" in {
    val map: Map[Int, Either[String, Int]] = Map(
      1 -> Right(1),
      2 -> Right(2)
    )

    map.liftMapToEither() shouldBe Right(Map(1 -> 1, 2 -> 2))
  }

  it should "lift map with left to left with the same value" in {
    val map: Map[Int, Either[String, Int]] =
      Map(1 -> Left("error"))

    map.liftMapToEither() shouldBe Left("error")
  }

  it should "lift map with left and right to left" in {
    val map: Map[Int, Either[String, Int]] =
      Map(1 -> Right(1), 2 -> Left("error"))

    map.liftMapToEither() shouldBe Left("error")

  }

}
