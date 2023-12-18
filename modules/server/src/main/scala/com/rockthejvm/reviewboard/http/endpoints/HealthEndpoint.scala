package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

trait HealthEndpoint {
  val healthEndpoint = endpoint
    .tag("health")
    .name("health")
    .description("health check")
    .get
    .in("health")
    .out(plainBody[String])

}
