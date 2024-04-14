package com.github.tammo.yabt.cli

trait CommandLineInterface:

  /** Processes the input arguments.
    * @param input
    *   The arguments to process, e.g. the program arguments.
    * @return
    *   The result of the processing, which could be printed to a console.
    */
  def processArguments(input: Array[String]): String  // TODO make the errors explicitly type
