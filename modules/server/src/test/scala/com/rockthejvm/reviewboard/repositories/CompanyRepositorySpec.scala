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

object CompanyRepositorySpec extends ZIOSpecDefault {

  private val rtjvm = Company(1L, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")
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
      }
    ).provide(CompanyRepositoryLive.layer, dataSourceLayer, Repository.quillLayer, Scope.default)
  }

  def createContainer(): PostgreSQLContainer[Nothing] = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript("sql/companies.sql")
    container.start()
    container
  }

  def closeContainer(
      container: PostgreSQLContainer[
        Nothing
      ]
  ) = container.stop()

  def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(container.getJdbcUrl())
    dataSource.setUser(container.getUsername())
    dataSource.setPassword(container.getPassword())
    dataSource

  }

  val dataSourceLayer = ZLayer {
    for {
      container <- ZIO
        .acquireRelease(
          ZIO.attempt(createContainer())
        )(cont => ZIO.attempt(cont.stop()).ignoreLogged)
      dataSource <- ZIO.attempt(createDataSource(container))
    } yield dataSource
  }

}
