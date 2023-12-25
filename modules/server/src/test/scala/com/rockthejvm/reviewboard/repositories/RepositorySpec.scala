package com.rockthejvm.reviewboard.repositories

import org.testcontainers.containers.PostgreSQLContainer
import io.getquill.jdbczio.Quill.DataSource
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource
import io.getquill.autoQuote
import java.sql.SQLException
import zio.*

trait RepositorySpec {
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
