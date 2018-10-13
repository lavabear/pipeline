package io.inapinch.pipeline.ws.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import com.google.common.collect.Maps
import io.inapinch.pipeline.*
import io.inapinch.pipeline.db.PipelineDao
import io.inapinch.pipeline.operations.CommandUsage
import io.inapinch.pipeline.ws.WebApplication
import io.javalin.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

data class Status(val message: String, val timestamp: LocalDateTime = LocalDateTime.now())

class PipelineController(private val mapper: ObjectMapper,
                         private val dao: PipelineDao,
                         private val manager: OperationsManager) {

    companion object {
        private val LOG = LoggerFactory.getLogger(WebApplication::class.java)

        private const val STATIC_CONTENT_URL = "http://pinch-pipeline.s3-website-us-west-2.amazonaws.com"
    }

    private val reactContent: Supplier<String>
    private val commands: Supplier<List<CommandUsage>>
    private val reactStaticContent: MutableMap<String, Supplier<Pair<String, String>>>

    init {
        reactStaticContent = Maps.newConcurrentMap()
        commands = Suppliers.memoize { CommandUsage.all() }
        reactContent = Suppliers.memoizeWithExpiration({ get("")!!
                .string()
                .replace("<noscript>You need to enable JavaScript to run this app.</noscript>", "") },
                30, TimeUnit.MINUTES)
    }

    fun newRequest(context: Context) {
        val request : PipelineRequest = mapper.readValue(context.body())

        context.header("Location", manager.enqueue(request))
        context.status(202)
    }

    fun staticContent(context: Context) {
        val path = context.request().servletPath
        val static = reactStaticContent.getOrElse(path) {
            val result = Suppliers.memoizeWithExpiration({
                val response = get(path)!!
                Pair(response.contentType().toString(), response.string()) }, 30, TimeUnit.MINUTES)
            reactStaticContent[path] = result
            result}.get()
        if(context.status() <= 400)
            context.status(200)
        context.contentType(static.first)
        context.result(static.second)
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

    private fun get(url: String, timeout: Duration = Duration.ofMinutes(1)): ResponseBody? = OkHttpClient.Builder()
            .readTimeout(timeout.toMinutes(), TimeUnit.MINUTES)
            .build()
            .newCall(Request.Builder().get().url("$STATIC_CONTENT_URL$url").build())
            .execute()
            .body()
}

data class PipelineError(val error: String)