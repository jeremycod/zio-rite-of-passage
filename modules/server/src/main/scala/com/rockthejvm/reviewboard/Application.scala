package com.rockthejvm.reviewboard

import zio.*
import zio.http.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

import com.rockthejvm.reviewboard.http.controllers.HealthController
import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.services.CompanyService
import com.rockthejvm.reviewboard.services.CompanyServiceLive
import com.rockthejvm.reviewboard.repositories.CompanyRepositoryLive
import com.rockthejvm.reviewboard.repositories.Repository
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase

object Application extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    server <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default // can add configs e.g. CORS
      ).toHttp(endpoints)
    )
    _ <- Console.printLine("Server started")
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    serverProgram.provide(
      Server.default,
      CompanyServiceLive.layer,
      CompanyRepositoryLive.layer,
      // other requirements
      Repository.dataLayer
    )

}
