package org.example.xstreams.impl

import org.example.xstreams.api.{FiniteXStream, XStream}
import org.example.xstreams.impl.XStreams.fromIterator

private class NoEmptyXStream[T](elem: => T, next: => XStream[T])
    extends FiniteXStream[T] {

  private def head: T = elem

  private def tail: XStream[T] = next

  override def take(nbr: Int): FiniteXStream[T] =
    if nbr == 0 then new EmptyXStream
    else new NoEmptyXStream(elem, tail.take(nbr - 1))

  override def filter(predicate: T => Boolean): XStream[T] =
    if (predicate(elem)) new NoEmptyXStream(elem, tail.filter(predicate))
    else tail.filter(predicate)

  override def map[B](mapping: T => B): XStream[B] =
    new NoEmptyXStream(mapping(elem), tail.map(mapping))

  override def concat(other: => XStream[T]): XStream[T] =
    new NoEmptyXStream[T](elem, tail.concat(other))

  override def concat(other: FiniteXStream[T]): FiniteXStream[T] =
    new NoEmptyXStream[T](elem, tail.concat(other))

  override def flatten[B](implicit
                          asIterableOnce: T => IterableOnce[B]
  ): XStream[B] =
    fromIterator(asIterableOnce(head).iterator)
      .concat(next.flatten(asIterableOnce))

  override def skip(nbr: Int): XStream[T] =
    if nbr == 0 then this
    else tail.skip(nbr - 1);

  override def takeWhile(predicate: T => Boolean): XStream[T] =
    if (predicate(elem)) new NoEmptyXStream(elem, tail.takeWhile(predicate))
    else new EmptyXStream

  override def skipWhile(predicate: T => Boolean): XStream[T] =
    if (predicate(elem)) tail.skipWhile(predicate)
    else this

  override def foldRight[B](initial: B, combinator: (B, T) => B): B =
    tail match {
      case x: FiniteXStream[T] =>
        combinator(x.foldRight(initial, combinator), elem)
      case _ => throw RuntimeException("Not supported operation")
    }

  override def iterator: Iterator[T] = {
    class IteratorImpl(var stream: NoEmptyXStream[T]) extends Iterator[T] {
      var hasMoreElements: Boolean = true

      override def hasNext: Boolean = hasMoreElements

      override def next(): T = {
        val r = stream.head
        stream.tail match {
          case y: EmptyXStream[T] =>
            hasMoreElements = false
          case tail: NoEmptyXStream[T] =>
            stream = tail
        }
        r
      }
    }
    new IteratorImpl(this)
  }

  override def zip[B](other: XStream[B]): XStream[(T, B)] = other match
    case e: EmptyXStream[B] => new EmptyXStream
    case ne: NoEmptyXStream[B] =>
      new NoEmptyXStream[(T, B)]((elem, ne.head), tail.zip(ne.tail))

  override def window(windowSize: Int): XStream[FiniteXStream[T]] =
    new NoEmptyXStream[FiniteXStream[T]](
      take(windowSize),
      tail.skip(windowSize - 1).window(windowSize)
    )

}
