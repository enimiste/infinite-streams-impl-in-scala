package org.example
package xstreams

trait XStream[T] {

  def take(nbr: Int): XStream[T]

  def takeWhile(predicate: T => Boolean): XStream[T]

  def skip(nbr: Int): XStream[T]

  def skipWhile(predicate: T => Boolean): XStream[T]

  def filter(predicate: T => Boolean): XStream[T]

  def map[B](mapping: T => B): XStream[B]

  def flatMap[B](mapping: T => XStream[B]): XStream[B]

  def concat(other: XStream[T]): XStream[T]

  def iterator: Iterator[T]

  //Terminal operations
  def forEach(consumer: T => Unit): Unit

  def reduce[B](initial: B, combinator: (B, T) => B): B

  def collect[B[_]](bag: B[T], collector: (B[T], T) => B[T]): B[T] =
    reduce(bag, collector)

  def toList: List[T] = collect(List[T](), (list, item) => list ++ List(item))

  def groupBy[K](keyGenerator: T => K): Map[K, List[T]] = {
    reduce(Map[K, List[T]](), (groups, x) => {
      val k: K = keyGenerator(x)
      val v: List[T] = groups.getOrElse(k, List[T]())
      val nv: List[T] = v ++ List(x)
      groups.+((k, nv))
    })
  }
}
