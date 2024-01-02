package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait ReviewRepository {
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]

}

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {
  import quill.*

  inline given reviewSchema: SchemaMeta[Review]     = schemaMeta[Review]("reviews")
  inline given reviewInsertMeta: InsertMeta[Review] = insertMeta[Review](_.id, _.created, _.updated)
  inline given reviewUpdateMeta: UpdateMeta[Review] =
    updateMeta[Review](_.id, _.companyId, _.userId, _.created)

  override def delete(id: Long): Task[Review] =
    run(query[Review].filter(_.id == lift(id)).delete.returning(r => r))

  override def create(review: Review): Task[Review] = run(
    query[Review].insertValue(lift(review)).returning(r => r)
  )

  override def getByCompanyId(id: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(id)))

  override def update(id: Long, op: Review => Review): Task[Review] =
    for {
      current <- getById(id).someOrFail(
        new RuntimeException(s"update review failed: missing id $id")
      )
      updated <- run(
        query[Review].filter(_.id == lift(id)).updateValue(lift(op(current))).returning(r => r)
      )
    } yield updated

  override def getById(id: Long): Task[Option[Review]] =
    run(query[Review].filter(_.id == lift(id))).map(_.headOption)

  override def getByUserId(userId: Long): Task[List[Review]] = run(
    query[Review].filter(_.userId == lift(userId))
  )

}
object ReviewRepositoryLive {
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase]].map(quill => ReviewRepositoryLive(quill))
  }

}
