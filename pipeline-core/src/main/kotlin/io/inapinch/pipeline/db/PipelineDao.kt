package io.inapinch.pipeline.db

import io.inapinch.pipeline.PipelineRequest
import io.inapinch.pipeline.scheduling.ScheduledItem
import java.util.*

interface PipelineDao {
    fun saveRequest(id: UUID, request: PipelineRequest)

    fun saveResult(id: UUID, result: Any, requestId: UUID)

    fun result(id: UUID) : Optional<Any>

    fun request(id: UUID) : Optional<PipelineRequest>

    fun saveScheduledItem(id: UUID, cron: String, requestId: UUID)

    fun scheduledItems() : List<ScheduledItem>

    fun saveAuthToken(authToken: String)

    fun validAuthToken(authToken: String) : Boolean

    companion object {
        lateinit var instance: PipelineDao
    }
}