package io.inapinch.pipeline.db

import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.inapinch.pipeline.PipelineRequest
import io.inapinch.pipeline.scheduling.ScheduleType
import io.inapinch.pipeline.scheduling.ScheduledItem
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*
import javax.sql.DataSource

class PipelineDaoImpl(private val objectMapper: ObjectMapper, dataSource: DataSource) : PipelineDao {

    override fun saveRequest(id: UUID, request: PipelineRequest) {
        val value = objectMapper.writeValueAsString(request)
        transaction {
            VerifiedPipelineRequests.insert {
                it[uuid] = id
                it[VerifiedPipelineRequests.request] = value
                it[created] = DateTime.now()
            }
        }
    }

    override fun request(id: UUID) : Optional<PipelineRequest> {
        var result : Optional<PipelineRequest> = Optional.empty()
        transaction {
            result = Optional.ofNullable(VerifiedPipelineRequests.select { VerifiedPipelineRequests.uuid eq id }.firstOrNull())
                .map { objectMapper.readValue(it[VerifiedPipelineRequests.request]) as PipelineRequest }
        }
        return result
    }

    override fun saveResult(id: UUID, result: Any, requestId: UUID) {
        val value = objectMapper.writeValueAsString(result)
        transaction {
            VerifiedPipelineResults.insert {
                it[uuid] = id
                it[this.result] = value
                it[this.requestId] = requestId
                it[created] = DateTime.now()
            }
        }
    }

    override fun result(id: UUID) : Optional<Any> {
        var result : Optional<Any> = Optional.empty()
        transaction {
            result = Optional.ofNullable(VerifiedPipelineResults.select {
                VerifiedPipelineResults.uuid eq id
            }.firstOrNull()).map { objectMapper.readValue(it[VerifiedPipelineResults.result]) as Any }
        }
        return result
    }

    override fun saveScheduledItem(id: UUID, cron: String, requestId: UUID) {
        transaction {
            ScheduledItems.insert {
                it[uuid] = id
                it[this.cron] = cron
                it[this.requestId] = requestId
                it[created] = DateTime.now()
            }
        }
    }

    override fun scheduledItems(): List<ScheduledItem> {
        val parser = CronParser(CronDefinitionBuilder.defineCron().instance())
        return transaction {
            ScheduledItems.selectAll()
                    .map {
                        ScheduledItem(it[ScheduledItems.requestId],
                                ScheduleType.PIPELINE,
                                parser.parse(it[ScheduledItems.cron]))
                    }
        }
    }

    override fun saveAuthToken(authToken: String) {
        transaction {
            AuthTokens.insert {
                it[this.token] = authToken
                it[created] = DateTime.now()
            }
        }
    }

    override fun validAuthToken(authToken: String) : Boolean {
        return transaction {
            !AuthTokens.select(where = { AuthTokens.token eq authToken }).empty()
        }
    }

    init {
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(VerifiedPipelineRequests, VerifiedPipelineResults, ScheduledItems, AuthTokens)
        }
        PipelineDao.instance = this
    }
}
