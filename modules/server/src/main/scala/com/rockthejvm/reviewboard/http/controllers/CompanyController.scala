package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.domain.data.Company
import collection.mutable
import zio.*
import sttp.tapir.server.ServerEndpoint
import com.rockthejvm.reviewboard.services.CompanyService
/*import math.Numeric.Implicits.infixNumericOps
import math.Fractional.Implicits.infixFractionalOps
import math.Integral.Implicits.infixIntegralOps*/

class CompanyController private (service: CompanyService)
    extends BaseController
    with CompanyEndpoints {
  // TODO implementation
  // in-memory "database"

  // create
  val create: ServerEndpoint[Any, Task] = createEndpoint
    .serverLogicSuccess { req => service.create(req) }
  // getAll
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogicSuccess(_ => service.getAll)

  val getById = getByIdEndpoint.serverLogicSuccess(id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(service.getById)
      .catchSome { case _: NumberFormatException =>
        service.getBySlug(id)
      }
  )

  override val routes: List[ServerEndpoint[Any, Task]] = {
    List(create, getAll, getById)
  }

}

object CompanyController {
  val makeZIO = for {
    service    <- ZIO.service[CompanyService]
    controller <- ZIO.succeed(new CompanyController(service))
  } yield (controller)
}
