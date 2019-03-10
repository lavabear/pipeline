package io.inapinch.pipeline.ws

import com.fasterxml.jackson.databind.ObjectMapper
import io.inapinch.pipeline.db.PipelineDao
import io.inapinch.pipeline.scheduling.Scheduler
import io.inapinch.pipeline.ws.controllers.PipelineController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.json.JavalinJackson
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

data class WebApplication(override val kodein: Kodein) : KodeinAware {
    private val controller: PipelineController by kodein.instance()
    private val objectMapper: ObjectMapper by kodein.instance()
    private val port: Int by kodein.instance("port")
    private val pipelineDao: PipelineDao by kodein.instance()

    fun start() {
        Scheduler.start(pipelineDao)

        Javalin.create().apply {
            port(port)
            enableCorsForOrigin("*") // enables cors for the specified origin(s)

            JavalinJackson.configure(objectMapper)

            routes {
                path("pipeline") {
                    path("api") {
                        get("", controller::status)
                        post("", controller::newRequest)
                        post("cl", controller::newRequestFromCommandLanguage)
                        get("commands", controller::commands)

                        get(":id", controller::pipelineStatus)
                        path(":id") {
                            post("schedule", controller::newScheduledItem)
                            get("operations", controller::pipelineRequest)
                        }
                    }
                }
            }

            exception(Exception::class.java, controller::error)
        }.start()
    }
}