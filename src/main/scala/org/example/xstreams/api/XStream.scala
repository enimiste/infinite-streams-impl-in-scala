package org.example.xstreams.api

/**
 * XStream public API
 *
 * @tparam T
 */
trait XStream[T] {

  /**
   * Returns a new finite stream having at most nbr elements
   *
   * @param nbr
   * @return
   */
  def take(nbr: Int): FiniteXStream[T]

  /**
   * Returns a new stream having element that satisfy the predicate
   *
   * @param predicate
   * @return
   */
  def takeWhile(predicate: T => Boolean): XStream[T]

  /**
   * Returns a new stream with the nbr elements of the original stream skipped
   *
   * @param nbr
   * @return
   */
  def skip(nbr: Int): XStream[T]

  /**
   * Returns a new stream starting from the first element that don't match the predicate
   *
   * @param predicate
   * @return
   */
  def skipWhile(predicate: T => Boolean): XStream[T]

  /**
   * Returns a new stream with only elements that satisfy the given predicate
   *
   * @param predicate
   * @return
   */
  def filter(predicate: T => Boolean): XStream[T]

  /**
   * Returns a new stream where each element is transformed version of the original one (applying the given mapping function)
   *
   * @param mapping
   * @tparam B
   * @return
   */
  def map[B](mapping: T => B): XStream[B]

  /**
   * Returns a new stream that merges all the sub stream into one
   *
   * @param mapping
   * @tparam B
   * @return
   */
  def flatMap[B](mapping: T => XStream[B]): XStream[B]

  /**
   * Returns a new stream that concatenate this stream with another one
   *
   * @param other
   * @return
   */
  def concat(other: XStream[T]): XStream[T]

  /**
   * Returns a new stream where each element is a tuple of the elements of this stream and another.
   * The length of this new stream is the length of the smallest one.
   *
   * @param other
   * @tparam B
   * @return
   */
  def zip[B](other: XStream[B]): XStream[(T, B)]

  /**
   * Returns a new stream where each element is a finite stream of windowSize elements from this element
   *
   * @param windowSize
   * @return
   */
  def window(windowSize: Int): XStream[FiniteXStream[T]]

  /**
   * Returns a new stream that execute the given consumer on each element
   *
   * @param consumer
   * @return
   */
  def peek(consumer: T => Unit): XStream[T] =
    map(item => {
      consumer(item)
      item
    })
}


