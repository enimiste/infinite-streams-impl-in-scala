package org.example.xstreams.impl

import org.example.xstreams.api.{FiniteXStream, XStream}

private class EmptyXStream[T] extends FiniteXStream[T] {

  def tail: XStream[T] = this

  override def take(nbr: Int): FiniteXStream[T] = this

  override def filter(predicate: T => Boolean): XStream[T] = this

  override def map[B](mapping: T => B): XStream[B] = new EmptyXStream

  override def concat(other: => XStream[T]): XStream[T] = other

  override def concat(other: FiniteXStream[T]): FiniteXStream[T] = other

  override def skip(nbr: Int): XStream[T] = this

  override def takeWhile(predicate: T => Boolean): XStream[T] = this

  override def skipWhile(predicate: T => Boolean): XStream[T] = this

  override def foldRight[B](initial: B, combinator: (B, T) => B): B = initial

  override def iterator: Iterator[T] = Iterator.empty

  override def zip[B](other: XStream[B]): XStream[(T, B)] = new EmptyXStream

  override def window(windowSize: Int): XStream[FiniteXStream[T]] =
    new EmptyXStream

  override def reversed: FiniteXStream[T] = this

  override def flatten[B](implicit
      asIterableOne: T => IterableOnce[B]
  ): XStream[B] = new EmptyXStream
}
