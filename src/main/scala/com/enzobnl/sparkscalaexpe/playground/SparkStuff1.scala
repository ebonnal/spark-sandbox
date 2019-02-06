package com.enzobnl.sparkscalaexpe.playground
import org.apache.http.protocol.ExecutionContext
import sun.misc.Regexp

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
/*
Case classes can be seen as plain and immutable data-holding objects that should exclusively depend on their constructor arguments.
 */
trait Iterator[A] {
  def hasNext: Boolean
  def next(a: A): A
}
class IntIterator[A](to: Int) extends Iterator[A]{
  private var current = 0
  override def hasNext: Boolean = current < to
  override def next(a: A): A =  {
    if (hasNext) {
//      val t = current
//      current += 1
      a
    } else a
  }
}

trait Pet {
  val name: String
}
trait PetGentil extends Pet {
  val doud: Boolean
}

class Cat(val name: String, val doud: Boolean) extends PetGentil
class Dog(val name: String, val doud: Boolean) extends PetGentil
trait T extends Cat{

}
object exercice2 extends Runnable {
  private var _ss = Seq.apply(1,2)
  def ss_=( s: Seq[Int]): Unit = {this._ss=s}

  val e = 7
  val a = () => this.e
  abstract class Notification

  case class Email(sender: String, title: String, body: String) extends Notification

  case class SMS(caller: String, message: String) extends Notification

  case class VoiceRecording(contactName: String, link: String) extends Notification


  def methodTest(x: Int)(s: String): String = x + s
  def f1(x: Int): Int = x*8
  var f2: (Int => Int) => Int = (x: Int=>Int) => x(8)

  object CustomerID {

    def apply(name: String) = s"$name--${Math.abs(Random.nextLong)}"

    def unapply(customerID: String): Option[(String, String)] = {
      val stringArray: Array[String] = customerID.split("--")
      if (stringArray.tail.nonEmpty) Some((stringArray(0), stringArray(1))) else None
    }
  }

  val customer1ID = CustomerID("Sukyoung")  // Sukyoung--23098234908
  customer1ID match {
    case CustomerID(name, _) => println(name) // prints Sukyoung
    case _ => println("Could not extract a CustomerID")
  }
  class Stack[A](private var elements: List[A] = Nil) {
    def push(x: A) { elements = x +: elements }
    def peek: A = elements.head
    def pop(): A = {
      val currentTop = peek
      elements = elements.tail
      currentTop
    }
    def +(other: Stack[A]): Stack[A] ={
      var l: List[A] = this.elements :+ pop
      new Stack[A]()
    }
  }

    case  class User(name: String, var age: Int){
    var age_ = age
    def a = age_ = age_ + 1
  }
  abstract class Animal {
    def name: String
  }
  case class Cat(name: String) extends Animal
  case class Dog(name: String) extends Animal


  override def run: Unit={
    print(78)
  }
}