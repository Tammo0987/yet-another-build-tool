package com.github.tammo.yabt.extensions

object SeqExtensions {

  extension [E, V](set: Seq[Either[E, V]]) {

    def liftSeqToEither(): Either[E, Seq[V]] =
      set.foldLeft[Either[E, Seq[V]]](Right(Seq.empty)) { (acc, e) =>
        for {
          value <- e
          seq <- acc
        } yield seq :+ value
      }

  }

}
