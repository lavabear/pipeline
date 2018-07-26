package io.inapinch.ws.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.inapinch.db.PipelineDao
import io.inapinch.pipeline.OperationsManager
import io.inapinch.pipeline.PipelineRequest
import io.inapinch.pipeline.PipelineStatus
import io.inapinch.ws.WebApplication
import io.javalin.Context
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

data class Status(val message: String, val timestamp: LocalDateTime = LocalDateTime.now())

object PipelineController {
    private val LOG = LoggerFactory.getLogger(WebApplication::class.java)

    private lateinit var dao: PipelineDao
    private lateinit var manager: OperationsManager
    private lateinit var mapper: ObjectMapper

    fun newRequest(context: Context) {
        val request : PipelineRequest = mapper.readValue(context.body())

        context.header("Location", manager.enqueue(request))
        context.status(202)
    }

    fun pipelineStatus(context: Context) {
        context.json(manager.status(context.param("id") as String))
    }

    fun pipelineRequest(context: Context) {
        val uuid = context.param("id") as String
        context.json(dao.request(uuid).orElse(null) ?: PipelineStatus("Not Found", uuid))
    }

    fun error(e: Exception, context: Context) {
        LOG.error(e.localizedMessage, e)
        context.json(PipelineError(e.message ?: e.localizedMessage))
    }

    fun status(context: Context) {
        context.json(Status("ok"))
    }

    fun landingPage(context: Context) {
        context.renderFreemarker("index.ftl")
    }

    fun prepareController(mapper: ObjectMapper, dao: PipelineDao, manager: OperationsManager = OperationsManager(dao)) {
        this.mapper = mapper
        this.dao = dao
        this.manager = manager
    }
}

data class PipelineError(val error: String)