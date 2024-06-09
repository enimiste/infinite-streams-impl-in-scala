package org.example.xstreams.impl

import org.example.xstreams.api.{FiniteXStream, XStream}

object XStreams {

  /** Returns an empty stream
    *
    * @tparam T
    * @return
    */
  def empty[T]: FiniteXStream[T] = new EmptyXStream[T]

  /** Returns a finite stream that has one element
    *
    * @param elem
    * @tparam T
    * @return
    */
  def once[T](elem: T): FiniteXStream[T] =
    new NoEmptyXStream[T](elem, new EmptyXStream[T])

  /** Returns an infinite stream that always returns the same element
    *
    * @param elem
    * @tparam T
    * @return
    */
  def fixed[T](elem: T): XStream[T] =
    iterate(elem, identity)

  /** Returns an infinite stream that construct the next element from the last
    * one using the op function
    *
    * @param elem
    * @param op
    * @tparam T
    * @return
    */
  def iterate[T](elem: T, op: T => T): XStream[T] =
    new NoEmptyXStream[T](elem, iterate(op(elem), op))

  /** Returns an infinite stream that construct the element using the supplier
    *
    * @param supplier
    * @tparam T
    * @return
    */
  def generate[T](supplier: () => T): XStream[T] =
    iterate(supplier(), x => supplier())

  /** Returns an infinite stream built from a finite sequence using a circular
    * index
    *
    * @param elems
    * @tparam T
    * @return
    */
  def circular[T](elems: Seq[T]): XStream[T] =
    if (elems.isEmpty) empty
    else {
      case class A(elems: Seq[T], var cursor: Int) {
        def next: T = {
          val r = elems(cursor)
          cursor = (cursor + 1) % elems.size
          r
        }
      }
      val a = A(elems, 1)
      iterate(elems.head, x => a.next)
    }

  /** Returns a finite stream backed by the supplied sequence
    *
    * @param items
    * @tparam T
    * @return
    */
  def finite[T](items: Seq[T]): FiniteXStream[T] =
    circular(items).take(items.size)

  /** Returns a stream backed by the given iterator.
    *
    * @param iterator
    * @tparam T
    * @return
    */
  def fromIterator[T](iterator: Iterator[T]): XStream[T] =
    if iterator.hasNext then
      new NoEmptyXStream[T](iterator.next(), fromIterator(iterator))
    else new EmptyXStream[T]
}
