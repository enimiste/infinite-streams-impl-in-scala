package org.example.xstreams

import java.util.Comparator

trait XStreamOps {
  def empty[T]: XStream[T]

  def once[T](elem: T): XStream[T]

  def fixed[T](elem: T): XStream[T] =
    iterate(elem, identity)

  def iterate[T](elem: T, op: T => T): XStream[T]

  def generate[T](supplier: () => T): XStream[T] =
    iterate(supplier(), x => supplier())

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

  def finite[T](items: Seq[T]): XFiniteStream[T] =
    circular(items).take(items.size)
}

//Defines only intermediate operations
trait XStream[T] {

  def take(nbr: Int): XFiniteStream[T]

  def takeWhile(predicate: T => Boolean): XStream[T]

  def skip(nbr: Int): XStream[T]

  def skipWhile(predicate: T => Boolean): XStream[T]

  def filter(predicate: T => Boolean): XStream[T]

  def map[B](mapping: T => B): XStream[B]

  def flatMap[B](mapping: T => XStream[B]): XStream[B]

  def concat(other: XStream[T]): XStream[T]

  def zip[B](other: XStream[B]): XStream[(T, B)]

  def window(windowSize: Int): XStream[XFiniteStream[T]]

  def peek(consumer: T => Unit): XStream[T] =
    map(item => {
      consumer(item)
      item
    })
}


//Defines terminal operations
trait XFiniteStream[T] extends XStream[T] {
  def foldLeft[B](initial: B, combinator: (B, T) => B): B

  def foldRight[B](initial: B, combinator: (B, T) => B): B

  def collect[B[_]](bag: B[T], collector: (B[T], T) => B[T]): B[T] =
    foldLeft(bag, collector)

  def toList: List[T] = collect(List[T](), (list, item) => list ++ List(item))

  def groupBy[K, B](keyGenerator: T => K, initial: B, combinator: (B, T) => B): Map[K, B] =
    foldLeft(Map[K, B](), (groups, item) => {
      val k: K = keyGenerator(item)
      val v: B = groups.getOrElse(k, initial)
      val nv: B = combinator(v, item)
      groups.+((k, nv))
    })

  def groupBy[K](keyGenerator: T => K): Map[K, List[T]] =
    groupBy(keyGenerator, List[T](), (list, item) => list ++ List(item))

  def forEach(consumer: T => Unit): Unit

  def iterator: Iterator[T]

  def size: Int = {
    val countBag: Array[Int] = Array(0)
    forEach(n => {
      val oldValue = countBag(0)
      countBag(0) = oldValue + 1
    })
    countBag(0)
  }

  def max(comparator: Comparator[T]): Option[T] =
    min(comparator.reversed)

  def min(comparator: Comparator[T]): Option[T]

}