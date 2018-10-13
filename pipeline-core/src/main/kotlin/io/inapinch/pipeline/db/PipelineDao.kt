package io.inapinch.pipeline.db

import io.inapinch.pipeline.PipelineRequest
import java.util.*

interface PipelineDao {
    fun saveRequest(id: String, request: PipelineRequest)

    fun saveResult(id: String, result: Any)

    fun result(id: String) : Optional<Any>

    fun request(id: String) : Optional<PipelineRequest>

    companion object {
        lateinit var instance: PipelineDao
    }
}