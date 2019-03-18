package io.inapinch.pipeline.ws.controllers

import com.cronutils.model.Cron
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.base.Supplier
import com.google.common.base.Suppliers
import io.inapinch.pipeline.*
import io.inapinch.pipeline.db.PipelineDao
import io.inapinch.pipeline.operations.CommandUsage
import io.inapinch.pipeline.ws.WebApplication
import io.javalin.Context
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

data class Status(val message: String, val timestamp: LocalDateTime = LocalDateTime.now())

private val LOG = LoggerFactory.getLogger(WebApplication::class.java)

data class ScheduledItemRequest(val cron: Cron)

class PipelineController(private val mapper: ObjectMapper,
                         private val dao: PipelineDao,
                         private val manager: OperationsManager,
                         private val skipAuth: Boolean,
                         private val commands: Supplier<List<CommandUsage>> = Suppliers.memoize { CommandUsage.all() }) {

    fun newRequest(context: Context) {
        ifAuthenticated(context) {
            val request : PipelineRequest = addVariableHeaders(context) { request, binding ->
                request.copy(binding = binding)
            }
            request.copy()
            it.header("Location", manager.enqueue(request).location(it))
            it.status(202)
        }
    }

    private inline fun <reified T : HasBinding> addVariableHeaders(context: Context, copy: (request: T, binding: Map<String, Any>) -> T) : T {
        val request: T = mapper.readValue(context.body())
        val contextHeaders = context.headerMap()
        val requestHeaders = request.binding["headers"]
        if(requestHeaders != null
                && (requestHeaders as Map<String,String>).values.any { it.startsWith("$") }) {
            val binding = request.binding.toMutableMap()
            val bindingHeader = (request.binding["headers"] as Map<String,String>).toMutableMap()
            requestHeaders.filter { (k, _) -> k.startsWith("$") }
                    .filter { (k, _) -> contextHeaders.containsKey(k)}
                    .mapValues { (key, _) -> contextHeaders[key]!! }
                    .forEach { bindingHeader[it.key] = it.value }
            binding["headers"] = bindingHeader
            return copy(request, binding)
        }
        return request
    }

    fun newScheduledItem(context: Context) {
        ifAuthenticated(context) {
            val request : ScheduledItemRequest = mapper.readValue(it.body())
            val uuid = UUID.fromString(it.pathParam("id"))
            dao.saveScheduledItem(UUID.randomUUID(), request.cron.asString(), uuid)
            it.status(201)
        }
    }

    fun newRequestFromCommandLanguage(context: Context) {
        ifAuthenticated(context) {
            val request : PipelineCLRequest = addVariableHeaders(context) { request, binding ->
                request.copy(binding = binding)
            }
            it.header("Location", manager.enqueue(request.toPipelineRequest(mapper)).location(it))
            it.status(202)
        }
    }

    private fun UUID.location(context: Context) =  "${context.host()}/pipeline/api/$this"

    fun pipelineStatus(context: Context) {
        ifAuthenticated(context) {
            it.json(manager.status(UUID.fromString(it.pathParam("id"))))
        }
    }

    fun pipelineRequest(context: Context) {
        ifAuthenticated(context) {
            val uuid = UUID.fromString(it.pathParam("id"))
            val request = dao.request(uuid).orElse(null)
            val response = request ?: PipelineStatus("Not Found", uuid)
            it.json(response)
            it.status(if(request != null) 200 else 404)
        }
    }

    fun error(e: Exception, context: Context) {
        LOG.error(e.localizedMessage, e)
        context.attribute("errorMessage", e.message ?: e.localizedMessage)
        context.status(500)
    }

    fun unauthenticated(context: Context) {
        context.result("Please login to continue")
    }

    fun notFound(context: Context) {
        context.result("Resource does not exist")
    }

    fun serverError(context: Context) {
        context.result("Something went wrong: ${context.attribute("errorMessage") ?: "Check logs"}")
    }

    fun status(context: Context) {
        context.json(Status("ok"))
    }

    fun commands(context: Context) {
        context.json(commands.get())
    }

    private fun ifAuthenticated(context: Context, endpoint: (Context) -> Unit) {
        val token = context.header("authToken")
        if(skipAuth || (token != null && dao.validAuthToken(token)))
            endpoint(context)
        else
            context.status(401)
    }
}