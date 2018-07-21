package io.inapinch.ws

import io.inapinch.ws.controllers.PipelineController
import io.javalin.ApiBuilder.*
import io.javalin.Javalin;

class WebApplication {
    companion object {
        fun start(port: Int = 8080) {
            val app = Javalin.create().apply {
                enableStandardRequestLogging()
                enableDynamicGzip()
                port(port)
            }.start()

            app.routes {
                get(PipelineController::heartbeat)
                path("pipeline") {
                    path("api") {
                        get(PipelineController::status)
                        post(PipelineController::newRequest)
                    }
                }
            }
        }
    }
}