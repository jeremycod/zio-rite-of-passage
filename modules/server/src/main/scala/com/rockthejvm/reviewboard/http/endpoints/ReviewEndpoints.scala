package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto._
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.domain.data.Review

trait ReviewEndpoints {

  val createEndpoint =
    endpoint
      .tag("Reviews")
      .name("create")
      .description("Add a review for a company")
      .in("reviews")
      .post
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getByIdEndpoint =
    endpoint
      .tag("Reviews")
      .name("getById")
      .description("Get a review by its id")
      .in("reviews" / path[Long]("id"))
      .get
      .out(jsonBody[Option[Review]])

    val getByCompanyIdEndpoint =
      endpoint
        .tag("Reviews")
        .name("getByCompanyId")
        .description("Get a reviews for company")
        .in("reviews" / "company" / path[Long]("id"))
        .get
        .out(jsonBody[Option[Review]])

      val getByUserIdEndpoint =
        endpoint
          .tag("Reviews")
          .name("getByUserId")
          .description("Get a reviews for user")
          .in("reviews" / "user" / path[Long]("id"))
          .get
          .out(jsonBody[Option[Review]])

}
