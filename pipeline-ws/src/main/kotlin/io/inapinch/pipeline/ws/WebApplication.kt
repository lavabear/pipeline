package io.inapinch.pipeline.ws

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import com.fasterxml.jackson.databind.ObjectMapper
import io.inapinch.pipeline.db.PipelineDao
import io.inapinch.pipeline.scheduling.Scheduler
import io.inapinch.pipeline.ws.controllers.PipelineController
import io.javalin.Context
import io.javalin.ErrorHandler
import io.javalin.Handler
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.json.JavalinJackson
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import kotlin.reflect.KFunction1

data class WebApplication(override val kodein: Kodein) : KodeinAware {
    private val controller: PipelineController by kodein.instance()
    private val objectMapper: ObjectMapper by kodein.instance()
    private val port: Int by kodein.instance("port")
    private val pipelineDao: PipelineDao by kodein.instance()
    private val metrics: MetricRegistry by kodein.instance()

    private val timers: MutableMap<String, Timer> = mutableMapOf()
    private val errorMeters: MutableMap<String, Meter> = mutableMapOf()

    fun start() {
        Scheduler.start(pipelineDao)

        Javalin.create().apply {
            enableCorsForOrigin("*") // enables cors for the specified origin(s)

            JavalinJackson.configure(objectMapper)

            routes {
                path("pipeline") {
                    path("api") {
                        get(timed(controller::status))
                        post(timed(controller::newRequest))
                        post("cl", timed(controller::newRequestFromCommandLanguage))
                        get("commands", timed(controller::commands))

                        path(":id") {
                            get(timed(controller::pipelineStatus))
                            post("schedule", timed(controller::newScheduledItem))
                            get("operations", timed(controller::pipelineRequest))
                        }
                    }
                }
            }

            exception(Exception::class.java, controller::error)
            error(401, errors(controller::unauthenticated))
            error(404, errors(controller::notFound))
            error(500, errors(controller::serverError))
        }.start(port)
    }

    private fun timed(endpoint: KFunction1<Context, Unit>) : Handler {
        return Handler {
            timers.computeIfAbsent(endpoint.name, metrics::timer).time {
                endpoint(it)
            }
        }
    }

    private fun errors(endpoint: KFunction1<Context, Unit>) : ErrorHandler {
        return ErrorHandler {
            errorMeters.computeIfAbsent(endpoint.name, metrics::meter).mark()
            endpoint(it)
        }
    }
}