package com.rockthejvm.reviewboard.services

import zio.*
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import com.rockthejvm.reviewboard.services.ReviewService
import zio.test.*
import java.time.Instant
import io.getquill.autoQuote
import sourcecode.Text.generate

object ReviewServiceSpec extends ZIOSpecDefault {

  val goodReview = Review(
    id = 1L,
    companyId = 1L,
    userId = 1L,
    management = 5,
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 10,
    review = "all good",
    created = Instant.now(),
    updated = Instant.now()
  )

  val badReview = Review(
    id = 2L,
    companyId = 1L,
    userId = 1L,
    management = 1,
    culture = 1,
    salary = 1,
    benefits = 1,
    wouldRecommend = 1,
    review = "BAD BAD",
    created = Instant.now(),
    updated = Instant.now()
  )

  val stubRepoLayer = ZLayer.succeed {
    new ReviewRepository {

      override def create(review: Review): Task[Review] = ZIO.succeed(goodReview)

      override def getByCompanyId(id: Long): Task[List[Review]] = ZIO.succeed {
        if (id == 1) List(goodReview, badReview)
        else List()
      }

      override def getById(id: Long): Task[Option[Review]] = ZIO.succeed {
        id match {
          case 1 => Some(goodReview)
          case 2 => Some(badReview)
          case _ => None
        }
      }

      override def getByUserId(userId: Long): Task[List[Review]] = ZIO.succeed {
        if (userId == 1) List(goodReview, badReview)
        else List()
      }

      override def delete(id: Long): Task[Review] =
        getById(id).someOrFail(new RuntimeException(s" id $id not found"))
      override def update(id: Long, op: Review => Review): Task[Review] =
        getById(id).someOrFail(new RuntimeException(s" id $id not found")).map(op)

    }
  }
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewServiceSpec")(
      test("create") {
        for {
          service <- ZIO.service[ReviewService]
          review <- service.create(
            CreateReviewRequest(
              companyId = goodReview.companyId,
              management = goodReview.management,
              culture = goodReview.culture,
              salary = goodReview.salary,
              benefits = goodReview.benefits,
              wouldRecommend = goodReview.wouldRecommend,
              review = goodReview.review
            ),
            userId = 1L
          )
        } yield assertTrue(
          review.companyId == goodReview.companyId &&
            review.management == goodReview.management &&
            review.culture == goodReview.culture &&
            review.salary == goodReview.salary &&
            review.benefits == goodReview.benefits &&
            review.wouldRecommend == goodReview.wouldRecommend &&
            review.review == goodReview.review
        )
      },
      test("get by id") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getById(1L)
          reviewNotFound <- service.getById(999L)
        } yield assertTrue(
          review.contains(goodReview) && reviewNotFound.isEmpty
        )
      },
      test("get by company") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getByCompanyId(1L)
          reviewNotFound <- service.getByCompanyId(999L)
        } yield assertTrue(
          review.toSet == Set(goodReview, badReview) &&
            reviewNotFound.isEmpty
        )
      },
      test("get by user") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getByUserId(1L)
          reviewNotFound <- service.getByUserId(999L)
        } yield assertTrue(
          review.toSet == Set(goodReview, badReview) &&
            reviewNotFound.isEmpty
        )
      }
    ).provide(
      ReviewServiceLive.layer,
      stubRepoLayer
    )
}
