package com.github.tammo.yabt.extensions

import com.github.tammo.yabt.extensions.SetExtensions.liftToEither
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class SetExtensionsTest extends AnyFlatSpecLike with Matchers:

  it should "lift empty set to right of an empty set" in:
    val set: Set[Either[String, Int]] = Set.empty
    set.liftToEither() shouldBe Right(Set.empty)

  it should "lift set with right values to set with the same values" in:
    val set: Set[Either[String, Int]] = Set(Right(1), Right(2))
    set.liftToEither() shouldBe Right(Set(1, 2))

  it should "lift set with left value to the exact same left value" in:
    val set: Set[Either[String, Int]] = Set(Left("error"))
    set.liftToEither() shouldBe Left("error")

  it should "lift set with left and right values to the left value" in:
    val set: Set[Either[String, Int]] = Set(Left("error"), Right(1))
    set.liftToEither() shouldBe Left("error")

