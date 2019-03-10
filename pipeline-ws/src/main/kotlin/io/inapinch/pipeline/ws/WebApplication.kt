package io.inapinch.pipeline.ws

import com.fasterxml.jackson.databind.ObjectMapper
import io.inapinch.pipeline.ws.controllers.PipelineController
import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import io.javalin.core.util.Util
import io.javalin.translator.json.JavalinJacksonPlugin
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

data class WebApplication(override val kodein: Kodein) : KodeinAware {
    private val controller: PipelineController by kodein.instance()
    private val objectMapper: ObjectMapper by kodein.instance()
    private val port: Int by kodein.instance("port")

    fun start() {
        Util.noServerHasBeenStarted = false //  Hide Annoying Javalin Message, app takes more than a second to start

        Javalin.create().apply {
            port(port)
            enableStandardRequestLogging()
            enableDynamicGzip()
            enableCorsForOrigin("*") // enables cors for the specified origin(s)

            JavalinJacksonPlugin.configure(objectMapper)

            routes {
                path("pipeline") {
                    path("api") {
                        get(controller::status)
                        post(controller::newRequest)
                        post("cl", controller::newRequestFromCommandLanguage)
                        path("commands") {
                            get(controller::commands)
                        }
                        path(":id") {
                            get(controller::pipelineStatus)
                            path("operations") {
                                get(controller::pipelineRequest)
                            }
                        }
                    }
                }
            }

            exception(Exception::class.java, controller::error)
        }.start()
    }
}