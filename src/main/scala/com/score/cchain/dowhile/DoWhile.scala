package com.score.cchain.dowhile

case class Result(r: Int)

case class Message(m: String)

object DoWhile {
  val rl = List(Result(2), Result(5), Result(3), Result(7), Result(10))
  if (rl.exists(_.r >= 11)) {
    val s = rl.filter(_.r > 3).map(x => s"yes... ${x.r.toString}")
    println(s)
  } else {
    println("no...")
  }

  val ml = List(Message("a, b, c"), Message("r, f"), Message("j, l"))
  val fl = ml.flatMap(m => m.m.split(",").toList).filter(_.equalsIgnoreCase("a"))
  println(fl)

  val zl = List(Option(Message("era")), Option(Message("eee")), None, Option(Message("ja")))
  println(zl.flatten)

  val l1 = List(Result(3), Result(1), Result(5), Result(8))
  val l2 = List(Message("2"), Message("3"), Message("9"), Message("4"), Message("5"))

  val l3 = for {
    a <- l1
    b <- l2
    if a.r == b.m.toInt
  } yield b

  println(l3)
}
