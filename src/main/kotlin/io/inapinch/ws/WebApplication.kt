package io.inapinch.ws

import com.fasterxml.jackson.databind.ObjectMapper
import io.inapinch.db.PipelineDao
import io.inapinch.ws.controllers.PipelineController
import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import io.javalin.translator.json.JavalinJacksonPlugin

class WebApplication {
    companion object {
        fun start(pipelineDao: PipelineDao, objectMapper: ObjectMapper, port: Int) {
            Javalin.create().apply {
                enableStandardRequestLogging()
                enableDynamicGzip()
                enableCorsForOrigin("http://localhost:3000") // enables cors for the specified origin(s)

                port(port)

                PipelineController.prepareController(objectMapper, pipelineDao)

                routes {
                    get(PipelineController::landingPage)
                    path("pipeline") {
                        path("api") {
                            get(PipelineController::status)
                            post(PipelineController::newRequest)
                            path("commands") {
                                get(PipelineController::commands)
                            }
                            path(":id") {
                                get(PipelineController::pipelineStatus)
                                path("operations") {
                                    get(PipelineController::pipelineRequest)
                                }
                            }
                        }
                    }
                }

                error(404, PipelineController::staticContent)

                exception(Exception::class.java, PipelineController::error)
                JavalinJacksonPlugin.configure(objectMapper)
            }.start()
        }
    }
}