package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*
import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.syntax.assert
import org.testcontainers.containers.PostgreSQLContainer
import io.getquill.jdbczio.Quill.DataSource
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource
import io.getquill.autoQuote
import java.sql.SQLException

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {
  override val initScript: String = "sql/company.sql"

  private val rtjvm = Company(1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")

  private def genString() =
    scala.util.Random.alphanumeric.take(8).mkString
  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString(),
      name = genString(),
      url = genString()
    )
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
        } yield company
        program
          .assert {
            case Company(_, "rock-the-jvm", "Rock the JVM", "rockthejvm.com", _, _, _, _, _) => true
            case _ => false
          }
      },
      test("create a duplicate should error") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rtjvm)
          err     <- repo.create(rtjvm).flip
        } yield err
        program.assert(_.isInstanceOf[SQLException])
      },
      test("get by id and slug") {
        val program = for {
          repo          <- ZIO.service[CompanyRepository]
          company       <- repo.create(rtjvm)
          fetchedById   <- repo.getById(company.id)
          fetchedBySlug <- repo.getBySlug(company.slug)
        } yield (company, fetchedById, fetchedBySlug)
        program.assert { case (company, fetchedById, fetchedBySlug) =>
          fetchedById.contains(company) && fetchedBySlug.contains(company)
        }
      },
      test("update record") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(rtjvm)
          updated     <- repo.update(company.id, _.copy(url = "blog.rockthejvm.com"))
          fetchedById <- repo.getById(company.id)
        } yield (updated, fetchedById)
        program
          .assert {
            case (updated, fetchedById) => fetchedById.contains(updated)
            case _                      => false
          }
      },
      test("delete record") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(rtjvm)
          _           <- repo.delete(company.id)
          fetchedById <- repo.getById(company.id)
        } yield (fetchedById)
        program
          .assert(_.isEmpty)
      },
      test("get all records") {
        val program = for {
          repo             <- ZIO.service[CompanyRepository]
          companies        <- ZIO.collectAll((1 to 10).map(_ => repo.create(genCompany())))
          companiesFetched <- repo.get

        } yield (companies, companiesFetched)
        program.assert { case (companies, companiesFetched) =>
          companies.toSet == companiesFetched.toSet
        }
      }
    ).provide(CompanyRepositoryLive.layer, dataSourceLayer, Repository.quillLayer, Scope.default)
  }

}
