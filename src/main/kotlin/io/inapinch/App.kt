package io.inapinch

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.inapinch.db.DbConfig
import io.inapinch.ws.WebApplication

fun main(args: Array<String>) {
    val objectMapper = jacksonObjectMapper()
    val pipelineDao = DbConfig.pipelineDao(objectMapper)

    WebApplication.start(pipelineDao, objectMapper)
}