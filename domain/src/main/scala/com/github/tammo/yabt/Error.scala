package com.github.tammo.yabt

object Error {

  sealed trait ResolveError

  enum ReadError extends ResolveError:
    case FileError(message: String, underlying: Throwable)
    case ParseError(message: String, underlying: Throwable)
    case DecodingError(message: String, pathToRootString: Option[String])

  case class MissingField(field: String) extends ResolveError
  case class MissingReference(reference: String) extends ResolveError
  case class IllegalRootReference(path: Seq[String]) extends ResolveError
  case class CyclicReference(path: Seq[String]) extends ResolveError

}
