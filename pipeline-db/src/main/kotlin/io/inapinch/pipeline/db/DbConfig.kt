package io.inapinch.pipeline.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.net.URI
import javax.sql.DataSource

object DbConfig {
    fun dataSource(jdbcUrl: String): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        return HikariDataSource(config)
    }

    fun postgresJdbcUrl(dbUri: URI) : String {
        val username = dbUri.userInfo.split(":")[0]
        val password = dbUri.userInfo.split(":")[1]
        val dbUrl = "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}"
        return "$dbUrl?user=$username&password=$password"
    }
}