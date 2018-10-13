package io.inapinch.pipeline.ws

import io.inapinch.pipeline.ws.controllers.PipelineController
import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

data class WebApplication(override val kodein: Kodein) : KodeinAware {
    private val javalin : Javalin by kodein.instance()
    private val controller: PipelineController by kodein.instance()

    fun start() {
        javalin.apply {
            routes {
                get(controller::landingPage)
                path("pipeline") {
                    path("api") {
                        get(controller::status)
                        post(controller::newRequest)
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

            error(404, controller::staticContent)

            exception(Exception::class.java, controller::error)
        }.start()
    }
}