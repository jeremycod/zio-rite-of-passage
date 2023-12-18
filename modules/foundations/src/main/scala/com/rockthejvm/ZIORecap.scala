package com.rockthejvm

import zio.*

import scala.io.StdIn

object ZIORecap extends ZIOAppDefault {

  val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  val aFailure: ZIO[Any, String, Nothing]   = ZIO.fail("SOmething went wrong")
  // suspension/delay
  val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife)

  // map/flatMap
  val improveMOL  = meaningOfLife.map(_ * 2)
  val printingMOL = meaningOfLife.flatMap(mol => ZIO.succeed(println(mol)))
  val smallProgram = for {
    _    <- Console.printLine("What's your name")
    name <- ZIO.succeed(StdIn.readLine())
    _    <- Console.printLine(s"Welcome to ZIO, $name")
  } yield ()

  // error handling
  val anAttempt: ZIO[Any, Throwable, Int] = ZIO.attempt {
    println("Trying something")
    val string: String = null
    string.length
  }

  val catchError = anAttempt.catchAll(e => ZIO.succeed(s"Returnign some different Value"))
  val catchSelective = anAttempt.catchSome {
    case e: RuntimeException => ZIO.succeed(s"Ignoring runtime exception: $e")
    case _                   => ZIO.succeed("Ignoring everything else")
  }

  val delayedValue = ZIO.sleep(1.second) *> Random.nextIntBetween(0, 100)
  val aPair = for {
    a <- delayedValue
    b <- delayedValue
  } yield (a, b) // this takes 2 seconds

  val aPairPar = for {
    fibA <- delayedValue.fork // return some other effect which has a fiber
    fibB <- delayedValue.fork
    a    <- fibA.join
    b    <- fibB.join
  } yield (a, b) // this takes 1 second

  val interruptedFiber = for {
    fib <- delayedValue.onInterrupt(ZIO.succeed(println("I'm interrupted"))).fork
    _   <- ZIO.sleep(500.millis) *> ZIO.succeed(println("cancelling fiber")) *> fib.interrupt
    _   <- fib.join
  } yield ()

  val ignoredInterruption = for {
    fib <- ZIO
      .uninterruptible(delayedValue.onInterrupt(ZIO.succeed(println("I'm interrupted"))))
      .fork
    _ <- ZIO.sleep(500.millis) *> ZIO.succeed(println("cancelling fiber")) *> fib.interrupt
    _ <- fib.join
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    Console.printLine("Rock the JVM")
}
