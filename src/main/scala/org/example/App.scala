package org.example

import org.example.xstreams.api.*
import org.example.xstreams.impl.XStreams.*

import scala.util.Random

object App {
  def main(args: Array[String]): Unit = {
    testXStreamsApi()
  }

  /**
   * Examples of using the XStream API
   */
  private def testXStreamsApi(): Unit =

    val stream: XStream[Int] = iterate(0, x => x + 1)

    stream
      .filter(n => n % 2 == 0)
      .take(5)
      .forEach(println)
    println("-" * 20)

    stream
      .map(n => "A" * n)
      .take(4)
      .forEach(println)
    println("-" * 20)

    stream
      .flatMap(n => once("A" * n) concat fixed("B" * n).take(5))
      .take(5)
      .forEach(println)
    println("-" * 20)
    stream
      .skip(1000)
      .take(10)
      .forEach(println)
    println("-" * 20)

    stream
      .takeWhile(n => n <= 10)
      .take(100) // protection
      .forEach(println)
    println("-" * 20)

    stream
      .skipWhile(n => n <= 10)
      .take(5)
      .forEach(println)
    println("-" * 20)

    val sum = stream
      .take(10)
      .foldLeft(0, Math.addExact)
    println("Sum : " + sum)
    println("-" * 20)

    val prod = stream
      .take(10)
      .foldLeft(1, Math.multiplyExact)
    println("Product : " + prod)
    println("-" * 20)

    val list = stream
      .take(10)
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

    val oddEven = stream
      .take(10)
      .groupBy(n => n % 2 == 0)
    println(oddEven)
    println("-" * 20)

    stream
      .filter(n => n % 2 == 1)
      .zip(stream.filter(n => n % 2 == 0))
      .take(5)
      .forEach(println)
    println("-" * 20)

    val alphabet = iterate(0, n => n + 1).map[Char](n => (n % 26 + 65).toChar)
    val alphabetZip = alphabet.zip(
      stream
        .skipWhile(n => n < 5)
        .filter(n => n % 5 == 0)
    )
    println("-" * 20)

    alphabetZip.take(26).forEach(println)
    val alphabetZipGroup =
      alphabetZip.take(1000).groupBy(_._1, 0, (a, b) => a + b._2)
    println(alphabetZipGroup)
    println("-" * 20)

    println(stream.take(1_000_000).size) // should be 10
    println("-" * 20)

    val alphabet2 = circular(for i <- 0 to 25 yield (i + 65).toChar)
    alphabet2.take(30).forEach(println)
    println("-" * 20)

    stream
      .skip(10)
      .peek(item => println("Peek : " + item))
      .window(10)
      .take(5)
      .forEach(s => {
        s.forEach(n => print(n + ", "))
        println()
      })
    println("-" * 20)

    stream
      .peek(item => println("Peek _ : " + item))
      .take(10)
      .forEach(println)
    println("-" * 20)

    val randomStream = generate(() => Random().nextInt(10_000_000))
    val rndSum = randomStream
      .peek(println)
      .take(10)
      .foldLeft(0, Math.addExact)
    println("Random Sum : " + rndSum)
    println("-" * 20)

    println("Max : " + randomStream.take(1000).max((a, b) => a - b))
    println("Min : " + randomStream.take(1000).min((a, b) => a - b))
    println("-" * 20)

    println("-" * 20)
    val names = finite(Seq("A", "B", "C", "D", "E"))
    println(names.foldLeft("", (acc, s2) => acc + " " + s2))
    println(names.foldRight("", (acc, s2) => acc + " " + s2))

    println("-" * 20)
    stream
      .map(n => n + " ")
      .take(1_000)
      .reversed
      .take(10)
      .forEach(println)

    println("-" * 20)
    println(
      stream
        .take(100)
        .matchAll(_ % 2 == 0)
    )

    println("-" * 20)
    println(
      stream
        .take(100)
        .matchAny(_ % 2 == 0)
    )

}
