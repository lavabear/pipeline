package io.inapinch.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.inapinch.pipeline.PipelineRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*
import javax.sql.DataSource

interface PipelineDao {
    fun saveRequest(id: String, request: PipelineRequest)

    fun saveResult(id: String, result: Any)

    fun result(id: String) : Optional<Any>

    fun request(id: String) : Optional<PipelineRequest>
}

object DbConfig {
    fun dataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:${System.getenv("DATABASE_URL")}"
        return HikariDataSource(config)
    }

    fun pipelineDao(objectMapper: ObjectMapper) : PipelineDao = PipelineDaoImpl(objectMapper)
}

class PipelineDaoImpl(private val objectMapper: ObjectMapper) : PipelineDao {

    override fun saveRequest(id: String, request: PipelineRequest) {
        val value = objectMapper.writeValueAsString(request)
        transaction {
            VerifiedPipelineRequests.insert {
                it[uuid] = UUID.fromString(id)
                it[VerifiedPipelineRequests.request] = value
                it[created] = DateTime.now()
            }
        }
    }

    override fun request(id: String) : Optional<PipelineRequest> {
        var result : Optional<PipelineRequest> = Optional.empty()
        transaction {
            result = Optional.ofNullable(VerifiedPipelineRequests.select { VerifiedPipelineRequests.uuid.eq(UUID.fromString(id)) }.firstOrNull())
                    .map { objectMapper.readValue(it[VerifiedPipelineRequests.request]) as PipelineRequest }
        }
        return result
    }

    override fun saveResult(id: String, result: Any) {
        val value = objectMapper.writeValueAsString(result)
        transaction {
            VerifiedPipelineResults.insert {
                it[uuid] = UUID.fromString(id)
                it[VerifiedPipelineResults.result] = value
                it[created] = DateTime.now()
            }
        }
    }

    override fun result(id: String) : Optional<Any> {
        var result : Optional<Any> = Optional.empty()
        transaction {
            result = Optional.ofNullable(VerifiedPipelineResults.select {
                VerifiedPipelineResults.uuid.eq(UUID.fromString(id))
            }.firstOrNull()).map { objectMapper.readValue(it[VerifiedPipelineResults.result]) as Any }
        }
        return result
    }

    init {
        Database.connect(DbConfig.dataSource())

        transaction {
            SchemaUtils.create(VerifiedPipelineRequests, VerifiedPipelineResults)
        }
    }
}

object VerifiedPipelineRequests : Table() {
    val uuid = uuid("uuid").primaryKey()
    val request = text("request")
    val created = datetime("created").index()
}

object VerifiedPipelineResults : Table() {
    val uuid = uuid("uuid").primaryKey()
    val result = text("result")
    val created = datetime("created").index()
}