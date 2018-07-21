package io.inapinch.ws.controllers

import io.javalin.Context
import java.time.LocalDateTime

data class Status(val message: String, val timestamp: LocalDateTime = LocalDateTime.now())

object PipelineController {
    fun newRequest(context: Context) {

    }

    fun status(context: Context) {
        context.json(Status("ok"))
    }

    fun heartbeat(context: Context) {
        context.json(Status("ok"))
    }
}