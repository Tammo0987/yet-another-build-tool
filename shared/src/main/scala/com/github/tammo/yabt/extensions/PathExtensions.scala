package com.github.tammo.yabt.extensions

import java.nio.file.Path
import scala.annotation.targetName

object PathExtensions:

  extension (path: Path)

    @targetName("slash")
    def /(subPath: Path): Path = path.resolve(subPath)

    @targetName("slash")
    def /(subPath: String): Path = /(Path.of(subPath))

