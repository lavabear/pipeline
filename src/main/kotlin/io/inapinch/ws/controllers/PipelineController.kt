package io.inapinch.ws.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import com.google.common.collect.Maps
import io.inapinch.db.PipelineDao
import io.inapinch.pipeline.*
import io.inapinch.pipeline.operations.CommandUsage
import io.inapinch.ws.WebApplication
import io.javalin.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

data class Status(val message: String, val timestamp: LocalDateTime = LocalDateTime.now())

object PipelineController {
    private val LOG = LoggerFactory.getLogger(WebApplication::class.java)

    private lateinit var dao: PipelineDao
    private lateinit var manager: OperationsManager
    private lateinit var mapper: ObjectMapper
    private lateinit var reactContent: Supplier<String>
    private lateinit var commands: Supplier<List<CommandUsage>>
    private lateinit var reactStaticContent: MutableMap<String, Supplier<String>>

    fun newRequest(context: Context) {
        val request : PipelineRequest = mapper.readValue(context.body())

        context.header("Location", manager.enqueue(request))
        context.status(202)
    }

    fun staticContent(context: Context) {
        val path = context.request().servletPath
        val static = reactStaticContent.getOrElse(path) {
            val result = Suppliers.memoizeWithExpiration({ get(path)!!.string() }, 30, TimeUnit.MINUTES)
            reactStaticContent[path] = result
            result}
        if(static.get() != null)
            context.status(200)
        context.result(static.get() ?: "Not Found")
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

    fun commands(context: Context) {
        context.json(commands.get())
    }

    fun prepareController(mapper: ObjectMapper, dao: PipelineDao,
                          client: DestinationClient = RestDestinationClient(mapper, OkHttpClient.Builder().build()),
                          manager: OperationsManager = OperationsManager(dao, client)) {
        this.mapper = mapper
        this.dao = dao
        this.manager = manager
        reactStaticContent = Maps.newConcurrentMap()
        commands = Suppliers.memoize { CommandUsage.all() }
        this.reactContent = Suppliers.memoizeWithExpiration({ get("")!!
                .string()
                .replace("<noscript>You need to enable JavaScript to run this app.</noscript>", "") },
                30, TimeUnit.MINUTES)
    }

    private fun get(url: String, timeout: Duration = Duration.ofMinutes(1)): ResponseBody? = OkHttpClient.Builder()
            .readTimeout(timeout.toMinutes(), TimeUnit.MINUTES)
            .build()
            .newCall(Request.Builder().get().url("$STATIC_CONTENT_URL$url").build())
            .execute()
            .body()

    private const val STATIC_CONTENT_URL = "http://pinch-pipeline.s3-website-us-west-2.amazonaws.com"
}

data class PipelineError(val error: String)