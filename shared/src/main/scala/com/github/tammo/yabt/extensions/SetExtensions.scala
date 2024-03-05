package com.github.tammo.yabt.extensions

object SetExtensions {

  extension [E, V](set: Set[Either[E, V]]) {
    def liftSetToEither(): Either[E, Set[V]] =
      set.foldLeft[Either[E, Set[V]]](Right(Set.empty)) { (acc, e) =>
        for {
          value <- e
          set <- acc
        } yield set + value
      }
  }

}
