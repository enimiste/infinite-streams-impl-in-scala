package org.example
package xstreams

object XStreams {
  def once[T](elem: T): XStream[T] =
    new XNoEmptyStream[T](elem, new XEmptyStream[T])

  def fixed[T](elem: T): XStream[T] =
    new XNoEmptyStream[T](elem, fixed(elem))

  def iterate[T](elem: T, op: T => T): XStream[T] =
    new XNoEmptyStream[T](elem, iterate(op(elem), op))

  private class XNoEmptyStream[T](val elem: T, next: => XStream[T]) extends XStream[T] {

    private def tail: XStream[T] = next

    override def take(nbr: Int): XStream[T] =
      if nbr == 0 then new XEmptyStream
      else new XNoEmptyStream(elem, tail.take(nbr - 1))

    override def forEach(consumer: T => Unit): Unit = {
      consumer(elem)
      tail.forEach(consumer)
    }

    override def filter(predicate: T => Boolean): XStream[T] =
      if (predicate(elem)) new XNoEmptyStream(elem, tail.filter(predicate))
      else tail.filter(predicate)

    override def map[B](mapping: T => B): XStream[B] = new XNoEmptyStream(mapping(elem), tail.map(mapping))

    override def concat(other: XStream[T]): XStream[T] =
      new XNoEmptyStream[T](elem, tail.concat(other))

    override def flatMap[B](mapping: T => XStream[B]): XStream[B] = {
      mapping(elem) match {
        case x: XEmptyStream[B] => tail.flatMap(mapping)
        case e: XNoEmptyStream[B] => {
          new XNoEmptyStream[B](e.elem, e.tail.concat(tail.flatMap(mapping)))
        }
      }
    }

    override def skip(nbr: Int): XStream[T] =
      if nbr == 0 then this
      else tail.skip(nbr - 1);

    override def takeWhile(predicate: T => Boolean): XStream[T] =
      if (predicate(elem)) new XNoEmptyStream(elem, tail.takeWhile(predicate))
      else new XEmptyStream

    override def skipWhile(predicate: T => Boolean): XStream[T] =
      if (predicate(elem)) tail.skipWhile(predicate)
      else this

    override def reduce[B](initial: B, combinator: (B, T) => B): B =
      tail.reduce(combinator(initial, elem), combinator)

    override def iterator: Iterator[T] = {
      class IteratorImpl(var stream: XNoEmptyStream[T]) extends Iterator[T] {
        var hasMoreElements: Boolean = true

        override def hasNext: Boolean = hasMoreElements

        override def next(): T = {
          val r = stream.elem
          stream.tail match {
            case y: XEmptyStream[T] =>
              hasMoreElements = false
            case tail: XNoEmptyStream[T] =>
              stream = tail
          }
          r
        }
      }
      new IteratorImpl(this)
    }
  }

  private class XEmptyStream[T] extends XStream[T] {

    def tail: XStream[T] = new XEmptyStream

    override def take(nbr: Int): XStream[T] = new XEmptyStream

    override def forEach(consumer: T => Unit): Unit = ()

    override def filter(predicate: T => Boolean): XStream[T] = this

    override def map[B](mapping: T => B): XStream[B] = new XEmptyStream

    override def flatMap[B](mapping: T => XStream[B]): XStream[B] = new XEmptyStream

    override def concat(other: XStream[T]): XStream[T] = other

    override def skip(nbr: Int): XStream[T] = new XEmptyStream

    override def takeWhile(predicate: T => Boolean): XStream[T] = new XEmptyStream

    override def skipWhile(predicate: T => Boolean): XStream[T] = new XEmptyStream

    override def reduce[B](initial: B, combinator: (B, T) => B): B = initial

    override def iterator: Iterator[T] = Iterator.empty
  }
}
