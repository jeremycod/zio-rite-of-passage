package com.rockthejvm

import zio.{IO, Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}
import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.autoQuote

import java.sql.SQLException
import java.util.UUID

object QuillDemo extends ZIOAppDefault {

  private def createJob(parent: Job, id: Int): Job = {
    val uuid = UUID.randomUUID().toString
    Job(uuid, s"title:$id", s"url$id", s"company:$id", Some(parent.id))
  }
  private def createJobsBatch(startId: Int): Seq[Job] = {
    val parent =
      Job(UUID.randomUUID().toString, s"title:$startId", s"url$startId", s"company:$startId", None)
    (startId + 1 to startId + 5).foldLeft[Seq[Job]](Seq[Job](parent))((jobs, counter) => {
      val last   = jobs.last
      val newJob = createJob(last, counter)
      jobs :+ newJob
    })
  }

  val program = for {
    repo <- ZIO.service[JobRepository]
    _ <- ZIO.foreachDiscard(1 to 100000) { n =>
      for {
        batch <- ZIO.succeed(createJobsBatch(n * 1000))
        r     <- repo.insertMany(batch)
      } yield r
    }
  } yield ()
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program.provide(
    JobRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("mydbconf")
  )
}

trait JobRepository {
  def create(job: Job): Task[Job]
  def update(id: String, op: Job => Job): Task[Job]
  def delete(id: String): Task[Job]
  def getById(id: String): Task[Option[Job]]
  def get: Task[List[Job]]
  def insertMany(jobs: Seq[Job]): zio.ZIO[Any, java.sql.SQLException, List[Long]]
}

class JobRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends JobRepository {
  import quill.*

  inline given schema: SchemaMeta[Job]  = schemaMeta[Job]("jobs")
  inline given insMeta: InsertMeta[Job] = insertMeta[Job]()
  inline given updMeta: UpdateMeta[Job] = updateMeta[Job]()
  override def create(job: Job): Task[Job] =
    run {
      query[Job]
        .insertValue(lift(job))
        .returning(j => j)
    }

  override def update(id: String, op: Job => Job): Task[Job] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Could not update missing key $id"))
    updated <- run {
      query[Job]
        .filter(_.id == lift(id))
        .updateValue(lift(op(current)))
        .returning(j => j)
    }
  } yield updated

  override def delete(id: String): Task[Job] =
    run {
      query[Job]
        .filter(_.id == lift(id))
        .delete
        .returning(j => j)
    }

  override def getById(id: String): Task[Option[Job]] =
    run {
      query[Job]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def get: Task[List[Job]] = run(query[Job])

  override def insertMany(jobs: Seq[Job]) = run {
    quote {
      liftQuery(jobs).foreach(j => query[Job].insertValue(j))
    }
  }
}

object JobRepositoryLive {
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, JobRepositoryLive] = ZLayer {
    ZIO.serviceWith[Quill.Postgres[SnakeCase]](quill => JobRepositoryLive(quill))
  }
}
