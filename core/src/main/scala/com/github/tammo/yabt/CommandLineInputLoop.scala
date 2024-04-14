package com.github.tammo.yabt

import com.github.tammo.yabt.cli.CommandLineInterface
import org.slf4j.LoggerFactory

import java.util.Scanner
import scala.util.Using

class CommandLineInputLoop(commandLineInterface: CommandLineInterface):

  private lazy val logger = LoggerFactory.getLogger(getClass)

  def loop(): Nothing =
    Using.resource(Scanner(System.in)) { scanner =>
      while (scanner.hasNext())
        val next = scanner.nextLine()
        val arguments = next.split("\\s+")
        logger.info(commandLineInterface.processArguments(arguments))
    }

    throw new IllegalStateException("This state should not be reachable.")
