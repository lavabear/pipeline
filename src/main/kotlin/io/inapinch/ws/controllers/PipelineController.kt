package io.inapinch.ws.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.base.Suppliers
import io.inapinch.db.PipelineDao
import io.inapinch.pipeline.*
import io.inapinch.ws.WebApplication
import io.javalin.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

data class Status(val message: String, val timestamp: LocalDateTime = LocalDateTime.now())

object PipelineController {
    private val LOG = LoggerFactory.getLogger(WebApplication::class.java)

    private lateinit var dao: PipelineDao
    private lateinit var manager: OperationsManager
    private lateinit var mapper: ObjectMapper
    private lateinit var reactContent: Supplier<String>

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
        context.html(reactContent.get())
    }

    fun prepareController(mapper: ObjectMapper, dao: PipelineDao,
                          client: DestinationClient = RestDestinationClient(mapper, OkHttpClient.Builder().build()),
                          manager: OperationsManager = OperationsManager(dao, client)) {
        this.mapper = mapper
        this.dao = dao
        this.manager = manager
        this.reactContent = Suppliers.memoizeWithExpiration({ OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.MINUTES)
                .build()
                .newCall(Request.Builder()
                        .get().url("http://pinch-pipeline.s3-website-us-west-2.amazonaws.com")
                        .build())
                .execute().body()!!
                .string() }, 30, TimeUnit.MINUTES)
    }
}

data class PipelineError(val error: String)