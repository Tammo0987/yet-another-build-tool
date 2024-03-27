package com.github.tammo.yabt.extensions

import com.github.tammo.yabt.extensions.SeqExtensions.liftToEither
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class SeqExtensionsTest extends AnyFlatSpecLike with Matchers:

  it should "lift empty seq to right of an empty seq" in:
    val seq: Seq[Either[String, Int]] = Seq.empty
    seq.liftToEither() shouldBe Right(Seq.empty)

  it should "lift seq with right values to seq with the same values" in:
    val seq: Seq[Either[String, Int]] = Seq(Right(1), Right(2))
    seq.liftToEither() shouldBe Right(Seq(1, 2))

  it should "lift seq with left value to the exact same left value" in:
    val seq: Seq[Either[String, Int]] = Seq(Left("error"))
    seq.liftToEither() shouldBe Left("error")

  it should "lift seq with left and right values to the left value" in:
    val seq: Seq[Either[String, Int]] = Seq(Left("error"), Right(1))
    seq.liftToEither() shouldBe Left("error")

