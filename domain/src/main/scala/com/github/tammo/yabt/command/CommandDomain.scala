package com.github.tammo.yabt.command

object CommandDomain:

  case class Command[T](
      name: String,
      description: String,
      options: Set[Option[T, ?]],
      default: T,
      execution: T => CommandExecutionResult
  )

  // TODO think about it
  enum CommandExecutionResult:
    case Success
    case Error

  sealed trait Option[T, V]:
    val name: String
    val description: String
    val required: Boolean
    val validate: V => Either[String, V]
    val construct: (T, V) => T

  case class Flag[T](
      name: String,
      description: String,
      required: Boolean,
      validate: Boolean => Either[String, Boolean],
      construct: (T, Boolean) => T
  ) extends Option[T, Boolean]

  case class StringOption[T](
      name: String,
      description: String,
      required: Boolean,
      validate: String => Either[String, String],
      construct: (T, String) => T
  ) extends Option[T, String]

  case class IntOption[T](
      name: String,
      description: String,
      required: Boolean,
      validate: Int => Either[String, Int],
      construct: (T, Int) => T
  ) extends Option[T, Int]

