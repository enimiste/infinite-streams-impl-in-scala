# Kata : Implementing infinite streams in Scala

This a learning project.
This implementation isn't performant nor optimal.
Some operations throw `StackOverflowError` exceptions with big streams (ex: size, forEach).

## API :
To use the `XStream` stream api you should call methods defined by the `XStreams` object.
### Stream creation operations :
```scala
object XStreams {
  /**
   * Returns an empty stream
   *
   * @param T
   * @return
   */
  def empty[T]: XFiniteStream[T]

  /**
   * Returns a finite stream that has one element
   *
   * @param elem
   * @tparam T
   * @return
   */
  def once[T](elem: T): XFiniteStream[T]

  /**
   * Returns an infinite stream that always returns the same element
   *
   * @param elem
   * @tparam T
   * @return
   */
  def fixed[T](elem: T): XStream[T]

  /**
   * Returns an infinite stream that construct the next element from the last one using the op function
   *
   * @param elem
   * @param op
   * @tparam T
   * @return
   */
  def iterate[T](elem: T, op: T => T): XStream[T]

  /**
   * Returns an infinite stream that construct the element using the supplier
   *
   * @param supplier
   * @tparam T
   * @return
   */
  def generate[T](supplier: () => T): XStream[T]

  /**
   * Returns an infinite stream built from a finite sequence using a circular index
   *
   * @param elems
   * @tparam T
   * @return
   */
  def circular[T](elems: Seq[T]): XStream[T]

  /**
   * Returns a finite stream backed by the supplied sequence
   *
   * @param items
   * @tparam T
   * @return
   */
  def finite[T](items: Seq[T]): XFiniteStream[T]

  /** Returns a stream backed by the given iterator.
   *
   * @param iterator
   * @tparam T
   * @return
   */
  def fromIterator[T](iterator: Iterator[T]): XStream[T]
}
```
### Intermediate/Transformation operations :
```scala
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
  def take(nbr: Int): XFiniteStream[T]

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

  /** Returns a new stream that merges all the sub stream into one
   *
   * @param asIterableOne
   * @tparam B
   * @return
   */
  def flatten[B](implicit asIterableOne: T => IterableOnce[B]): XStream[B]

  /** Returns a new stream that merges all the sub stream into one, after
   * applying the mapping function to each one of the elements
   *
   * @param mapping
   * @tparam B
   * @return
   */
  def flatMap[B, C](mapping: T => B)(implicit asIterableOne: B => IterableOnce[C]): XStream[C]

  /**
   * Returns a new stream that concatenate this stream with another one
   *
   * @param other
   * @return
   */
  def concat(other: => XStream[T]): XStream[T]

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
  def window(windowSize: Int): XStream[XFiniteStream[T]]

  /**
   * Returns a new stream that execute the given consumer on each element
   *
   * @param consumer
   * @return
   */
  def peek(consumer: T => Unit): XStream[T]
}
```
### Terminal operations
```scala
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
  def foldLeft[B](initial: B, combinator: (B, T) => B): B

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
  def collect[B[_]](bag: B[T], collector: (B[T], T) => B[T]): B[T]

  /**
   * List implementation of the collect
   *
   * @return
   */
  def toList: List[T]

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
  def groupBy[K, B](keyGenerator: T => K, initial: B, combinator: (B, T) => B): Map[K, B]

  /**
   * List implementation of the groupBy
   *
   * @param keyGenerator
   * @tparam K
   * @return
   */
  def groupBy[K](keyGenerator: T => K): Map[K, List[T]]

  /**
   * Execute a given consumer over the elements of the stream
   *
   * @param consumer
   */
  def forEach(consumer: T => Unit): Unit

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
  def size: Int

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
  def max(comparator: Comparator[T]): Option[T]

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
  def min(comparator: Comparator[T]): Option[T]

  /**
   * Returns a new finite stream that traverse elements in the reverse order
   *
   * @return
   */
  def reversed: FiniteXStream[T]

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
  def matchAll(predicate: T => Boolean): Boolean

  /**
   * Returns True if any of the elements of this stream matches the given predicate. Otherwise it returns False.
   *
   * <p>It doesn't traverse all the stream in all the cases. It stops on the first element that don't match the predicate</p>
   *
   * @param predicate
   * @return
   */
  def matchAny(predicate: T => Boolean): Boolean
}

```


## Examples :
**Imports :**
```scala
import org.example.xstreams.api.*
import org.example.xstreams.impl.XStreams.*
```
**Create an infinite stream representing all integer numbers :**
```scala
val stream: XStream[Int] = iterate(0, x => x + 1)
```
**Filter only even numbers then print the first 10 numbers :**
```scala
stream
      .filter(n => n % 2 == 0)//Infinite stream
      .take(5)//Finite stream
      .forEach(println)
```
```text
0
2
4
6
8
```
**Flatmap a stream of streams :**
```scala
println(
  stream
    .skip(1)
    .flatMap(n => once("A" * n))
    .take(5)
    .toList
)
```
```text
List(A, AA, AAA, AAAA, AAAAA)
```

**More examples :**

Open this repository on any editor supporting scala (VS Code/Metal, Intellij Idea, GitPod, ...).

Then run the `App.scala` main class.
