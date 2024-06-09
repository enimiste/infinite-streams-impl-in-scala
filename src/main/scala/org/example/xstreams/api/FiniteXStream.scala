package org.example.xstreams.api

import org.example.xstreams.impl.XStreams

import java.util.Comparator
import scala.collection.mutable.ListBuffer

/**
 * Trait that defines operation that are safe to execute only (has also a meaning) on a finite stream
 * <p>NB : A finite stream extends the API of the infinite stream.</p>
 *
 * @tparam T
 */
trait FiniteXStream[T] extends XStream[T] {

  /**
   * Left reducer
   *
   * @param initial
   * @param combinator
   * @tparam B
   * @return
   */
  def foldLeft[B](initial: B, combinator: (B, T) => B): B = {
    val it = iterator
    var acc = initial
    while (it.hasNext) acc = combinator(acc, it.next())
    acc
  }

  /**
   * Right reducer
   * @param initial
   * @param combinator
   * @tparam B
   * @return
   */
  def foldRight[B](initial: B, combinator: (B, T) => B): B

  /**
   * Collect stream data into a bag (List, Set, ...)
   *
   * @param bag
   * @param collector
   * @tparam B
   * @return
   */
  def collect[B[_]](bag: B[T], collector: (B[T], T) => B[T]): B[T] =
    foldLeft(bag, collector)

  /**
   * List implementation of the collect
   *
   * @return
   */
  def toList: List[T] =
    collect(ListBuffer.empty[T], (list, item) => list += item).toList

  /**
   * Returns a Map of the grouped data
   *
   * @param keyGenerator
   * @param initial
   * @param combinator
   * @tparam K
   * @tparam B
   * @return
   */
  def groupBy[K, B](
                     keyGenerator: T => K,
                     initial: B,
                     combinator: (B, T) => B
                   ): Map[K, B] = {
    val groups = scala.collection.mutable.HashMap.empty[K, B]
    val it = iterator
    while (it.hasNext) {
      val item = it.next()
      val k: K = keyGenerator(item)
      val v: B = groups.getOrElse(k, initial)
      val nv: B = combinator(v, item)
      groups.put(k, nv)
    }

    groups.toMap
  }

  /**
   * List implementation of the groupBy
   *
   * @param keyGenerator
   * @tparam K
   * @return
   */
  def groupBy[K](keyGenerator: T => K): Map[K, List[T]] =
    groupBy(keyGenerator, List[T](), (list, item) => list ++ List(item))

  /**
   * Execute a given consumer over the elements of the stream
   *
   * @param consumer
   */
  def forEach(consumer: T => Unit): Unit = {
    val it = iterator
    while (it.hasNext) consumer(it.next())
  }

  /**
   * Returns an iterator over the elements of  the stream
   *
   * @return
   */
  def iterator: Iterator[T]

  /**
   * Returns the element count of this stream
   *
   * @return
   */
  def size: Int = {
    val it = iterator
    var count = 0
    while (it.hasNext) {
      count += 1
      it.next()
    }
    count
  }

  /**
   * Returns the biggest element of this stream using the given comparator.
   * <ul>
   *   <li>None if the stream is empty.</li>
   *   <li>The biggest even there is duplicates.</li>
   * </ul>
   *
   * @param comparator
   * @return
   */
  def max(comparator: Comparator[T]): Option[T] =
    min(comparator.reversed)

  /**
   * Returns the smallest element of this stream using the given comparator.
   * <ul>
   * <li>None if the stream is empty.</li>
   * <li>The smallest even there is duplicates.</li>
   * </ul>
   *
   * @param comparator
   * @return
   */
  def min(comparator: Comparator[T]): Option[T] = {
    val it = iterator
    if (!it.hasNext) return None
    var min = it.next()
    while (it.hasNext) {
      val item = it.next()
      if (comparator.compare(item, min) < 0) min = item
    }
    Some(min)
  }

  /**
   * Returns a new finite stream that traverse elements in the reverse order
   *
   * @return
   */
  def reversed: FiniteXStream[T] = XStreams.finite(toList.reverse)

  /**
   * Returns a new finite stream that concatenate this stream with another one.
   *
   *<p>This overload of the concat method, force the return type to be FiniteXStream if the other stream is also finite</p>
   *
   * @param other
   * @return
   */
  def concat(other: FiniteXStream[T]): FiniteXStream[T]

  /**
   * Returns True if all the elements of this stream matches the given predicate. Otherwise it returns False
   * <p>It doesn't traverse all the stream in all cases. It stops on the first element that don't match the predicate</p>
   *
   * @param predicate
   * @return
   */
  def matchAll(predicate: T => Boolean): Boolean = {
    val it = iterator
    while (it.hasNext) {
      if (!predicate(it.next())) return false
    }
    true
  }

  /**
   * Returns True if any of the elements of this stream matches the given predicate. Otherwise it returns False.
   *
   * <p>It doesn't traverse all the stream in all the cases. It stops on the first element that don't match the predicate</p>
   *
   * @param predicate
   * @return
   */
  def matchAny(predicate: T => Boolean): Boolean = {
    val it = iterator
    while (it.hasNext) {
      if (predicate(it.next())) return true
    }
    false
  }
}
