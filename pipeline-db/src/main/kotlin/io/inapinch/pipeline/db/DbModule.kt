package io.inapinch.pipeline.db

import org.kodein.di.Kodein.Module
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import java.net.URI
import javax.sql.DataSource

object DbModule {
    val module = Module("DbModule") {
        bind<URI>(tag = "db-uri") with provider {  URI(System.getenv("DATABASE_URL")) }
        bind<String>(tag = "postgres") with provider {  DbConfig.postgresJdbcUrl(instance("db-uri")) }
        bind<DataSource>() with provider { DbConfig.dataSource( instance("postgres")) }
        bind<PipelineDao>() with singleton { PipelineDaoImpl(instance(), instance()) }
    }
}