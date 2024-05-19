# Scala learning by doing

```scala
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
```

## Usage :
```scala
object App {
  def main(args: Array[String]): Unit = {
    import org.example.xstreams.*
    import org.example.xstreams.XStreams.*

    val stream: XStream[Int] = iterate(1, x => x + 1)

    stream.filter(n => n % 2 == 0)
      .take(5)
      .forEach(println)
    println("-" * 20)
    stream.take(5)
      .filter(n => n % 2 == 0)
      .forEach(println)
    println("-" * 20)
    stream.map(n => "A" * n)
      .take(4)
      .forEach(println)
    println("-" * 20)
    stream.flatMap(n => once("A" * n) concat fixed("B" * n).take(3))
      .take(5)
      .forEach(println)
    println("-" * 20)
    stream.skip(1000)
      .take(10)
      .forEach(println)
    println("-" * 20)
    stream.takeWhile(n => n <= 10)
      .take(100) //protection
      .forEach(println)
    println("-" * 20)
    stream.skipWhile(n => n <= 10)
      .take(5)
      .forEach(println)
    println("-" * 20)
    val sum = stream.take(10)
      .reduce(0, Math.addExact)
    println("Sum : " + sum)
    println("-" * 20)
    val prod = stream.take(10)
      .reduce(1, Math.multiplyExact)
    println("Product : " + prod)
    println("-" * 20)
    val list = stream.take(10)
      .collect(List[Int](), (list, item) => list ++ List(item))
    println("As List   : " + list)
    println("-" * 20)
    val list2 = stream.take(10).toList
    println("As List 2 : " + list2)
    println("-" * 20)
    for (item <- stream.take(10).iterator) println(item)
    println("-" * 20)
    for (item <- stream.take(10).iterator) println(item)
    println("-" * 20)
    val oddEven = stream.take(10)
      .groupBy(n => n % 2 == 0)
    println(oddEven)
  }
}

```