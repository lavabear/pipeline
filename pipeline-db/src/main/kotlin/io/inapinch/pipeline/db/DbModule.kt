package io.inapinch.pipeline.db

import org.kodein.di.Kodein.Module
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import javax.sql.DataSource

object DbModule {
    val module = Module("DbModule") {
        bind<URI>(tag = "db-uri") with provider {  URI(System.getenv("DATABASE_URL")) }
        bind<String>(tag = "postgres") with provider {  DbConfig.postgresJdbcUrl(instance("db-uri")) }
        bind<DataSource>() with provider {
            dataSourceRetryOnFailure(instance("postgres")) {
                DbConfig.dataSource(it)
            }
        }
        bind<PipelineDao>() with singleton { PipelineDaoImpl(instance(), instance()) }
    }
}

private val LOG : Logger = LoggerFactory.getLogger(DbModule::class.java)

private val waitTimes : Map<Int, Long> = mapOf<Int, Long>(
        5 to 5_000,
        4 to 10_000,
        3 to 30_000,
        2 to 60_000,
        1 to 120_000
)

class StartupException(override val message: String?): Error()

private val databaseHostMatcher = """jdbc:\w+://(\w+)""".toRegex()

fun hostFromDatabaseUrl(
        databaseUrl: String, prefix: String = "from "
) : String = Optional.ofNullable(databaseHostMatcher.find(databaseUrl))
        .map { it.groupValues[1] }
        .map { "$prefix$it" }
        .orElse("")


fun dataSourceRetryOnFailure(
        databaseUrl: String,
        attempts: Int = 5, waitTimeMs: Long = waitTimes[attempts] ?: 1_000,
        dataSourceProvider: (String) -> DataSource = DbConfig::dataSource
) : DataSource {
    if(attempts <= 0)
        throw StartupException("Could not obtain database connection: ${hostFromDatabaseUrl(databaseUrl)}")

    return try {
        dataSourceProvider(databaseUrl)
    } catch (e: Throwable) {
        val remainingAttempts = attempts - 1
        LOG.warn("Failed to connect ", e)
        LOG.warn("remaining attempts: {} - retry in {} seconds", remainingAttempts, waitTimeMs / 1_000)
        Thread.sleep(waitTimeMs)
        dataSourceRetryOnFailure(databaseUrl, remainingAttempts)
    }
}