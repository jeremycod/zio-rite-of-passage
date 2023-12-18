package com.rockthejvm

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server
import zio.json.{DeriveJsonCodec, JsonCodec}
import com.rockthejvm.*

import scala.collection.mutable

object TapirDemo extends ZIOAppDefault {

  val simplestEndpoint = endpoint
    .tag("simple")
    .name("simple")
    .description("simplest endpoint possible")
    // ^^ for documentation
    .get
    .in("simple")           // path
    .out(plainBody[String]) // output
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  val simpleServerProgram = Server.serve(
    ZioHttpInterpreter(
      ZioHttpServerOptions.default // can add configs e.g. CORS
    ).toHttp(simplestEndpoint)
  )

  // simulate a job board
  val db: mutable.Map[Long, Job] = mutable.Map(
    1L -> Job("1L", "Instructor", "rockthejvm.com", "Rock the JVM", None)
  )

  val getAllEndpoint = endpoint
    .tag("jobs")
    .name("getAll")
    .description("Get all jobs")
    .in("jobs")
    .get
    .out(jsonBody[List[Job]])
    .serverLogicSuccess(_ => ZIO.succeed(db.values.toList))

  override def run = simpleServerProgram.provide(
    Server.default
  )

}
