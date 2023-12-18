package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.domain.data.Company
import collection.mutable
import zio.*
import sttp.tapir.server.ServerEndpoint

class CompanyController extends BaseController with CompanyEndpoints {
  // TODO implementation
  // in-memory "database"

  val db = mutable.Map[Long, Company](
    -1L -> Company(-1L, "invalid", "No Company", "nothing")
  )

  // create
  val create: ServerEndpoint[Any, Task] = createEndpoint
    .serverLogicSuccess { req =>
      ZIO.succeed {
        val newId      = db.keys.max + 1
        val newCompany = req.toCompany(newId)
        db += (newId -> newCompany)
        newCompany
      }
    }
  // getAll
  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogicSuccess(_ => ZIO.succeed(db.values.toList))

  val getById = getByIdEndpoint.serverLogicSuccess(id =>
    ZIO
      .attempt(id.toLong)
      .map(db.get)
  )

  override val routes: List[ServerEndpoint[Any, Task]] = {
    List(create, getAll, getById)
  }

}

object CompanyController {
  val makeZIO = ZIO.succeed(new CompanyController)
}
