package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.* 
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto._
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.domain.data.Company

trait CompanyEndpoints {
    val createEndpoint = endpoint
    .tag("companies")
    .name("create")
    .description("create a listing for a company")
    .in("companies")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

    val getAllEndpoint = endpoint
        .tag("companies")
        .name("getAll")
        .description("get all company listing")
        .in("companies")
        .get
        .out(jsonBody[List[Company]])

    val getByIdEndpoint =
        endpoint
            .tag("companies")
            .name("getById")
            .description("get company by its id (or maybe by slug?)")
            .in("companies" / path[String]("id"))
            .get
            .out(jsonBody[Option[Company]])
  
}
