package com.github.tammo.yabt.cli

import com.github.tammo.yabt.command.CommandDomain
import com.github.tammo.yabt.command.CommandDomain.*

import scala.annotation.tailrec
import scala.util.Try

class ConsoleCommandLineInterface(
    private val registeredCommands: Set[Command[?]]
) extends CommandLineInterface:

  override def processArguments(input: Array[String]): String = {
    if (input.length == 0) {
      return "invalid input, no command provided"
    }

    val commandName = input(0)
    val arguments = input.toSeq.tail

    val result = registeredCommands
      .find(_.name == commandName)
      .toRight(s"Unknown command $commandName")
      .flatMap { command =>
        validateCommandArgument(command, arguments).map(cc =>
          command.execution(cc)
        )
      }

    result match
      case Left(value) => value
      case Right(value) =>
        value match
          case CommandExecutionResult.Success =>
            s"successfully executed command $commandName"
          case CommandExecutionResult.Error => s"Execution error"
  }

  private def validateCommandArgument[T](
      command: Command[T],
      arguments: Seq[String]
  ): Either[String, T] = {

    @tailrec
    def parseArgument(
        last: Either[String, T],
        current: String,
        next: Seq[String]
    ): Either[String, T] = {
      if (current.startsWith("--")) {
        val parsedOption = parseOption(current, next.headOption.getOrElse(""))

        val name = parsedOption.name
        val value = parsedOption.value
        val option = command.options.find(_.name == name)

        val result = option
          .filter(_ => !parsedOption.spaceSeparated || next.nonEmpty)
          .toRight(s"Unknown parameter: $name")
          .flatMap {
            case Flag(_, _, _, validate, construct) =>
              last.flatMap { x =>
                Try(value.toBoolean).toEither.left
                  .map(_ => s"\"$value\" is not a boolean")
                  .flatMap(validate)
                  .map(construct(x, _))
              }

            case StringOption(_, _, _, validate, construct) =>
              last.flatMap { last =>
                // could be generalized the same as the others
                validate(value).map(construct(last, _))
              }

            case IntOption(_, _, _, validate, construct) =>
              last.flatMap { x =>
                Try(value.toInt).toEither.left
                  .map(_ => s"\"$value\" is not an int")
                  .flatMap(validate)
                  .map(construct(x, _))
              }
          }

        if (parsedOption.spaceSeparated) {
          if (next.tail.isEmpty) {
            result
          } else {
            parseArgument(result, next.tail.head, next.tail.tail)
          }
        } else {
          if (next.isEmpty) {
            result
          } else {
            parseArgument(result, next.head, next.tail)
          }
        }
      } else if (current.startsWith("-") && current.length == 2) {
        val flagName = current.slice(1, current.length)
        val option = command.options.find(_.name == flagName)

        val result = option
          .collect { case flag: Flag[T] =>
            flag
          }
          .toRight(s"Unknown flag: $flagName")
          .flatMap { f =>
            last.flatMap { l =>
              Right(true)
                .flatMap(f.validate)
                .map(b => f.construct(l, b))
            }
          }

        if (next.isEmpty) {
          result
        } else {
          parseArgument(result, next.head, next.tail)
        }
      } else {
        Left(s"malformed option $current")
      }
    }

    parseArgument(Right(command.default), arguments.head, arguments.tail)
  }

  private val equalRegex = "(\\w+)=(\\w+)".r
  private def parseOption(current: String, next: String): ParsedOption =
    current.slice(2, current.length) match {
      case equalRegex(name, value) => ParsedOption(name, value, false)
      case other                   => ParsedOption(other, next, true)
    }

  private case class ParsedOption(
      name: String,
      value: String,
      spaceSeparated: Boolean
  )
