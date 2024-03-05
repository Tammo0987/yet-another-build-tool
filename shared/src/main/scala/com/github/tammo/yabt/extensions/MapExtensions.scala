package com.github.tammo.yabt.extensions

object MapExtensions {

  extension [E, K, V](map: Map[K, Either[E, V]]) {
    def liftToEither(): Either[E, Map[K, V]] =
      map.foldLeft[Either[E, Map[K, V]]](Right(Map.empty)) { (acc, e) =>
        for {
          value <- e._2
          map <- acc
        } yield map + (e._1 -> value)
      }
  }

}
