package io.inapinch.pipeline.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.inapinch.pipeline.DestinationClient
import io.inapinch.pipeline.OperationsManager
import io.inapinch.pipeline.db.DbModule
import io.inapinch.pipeline.ws.controllers.PipelineController
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

val pipelineModule = Kodein.Module("pipelineModule") {
    bind<OkHttpClient>() with provider { OkHttpClient().newBuilder().build() }
    bind<DestinationClient>() with provider { DestinationClient.restClient(instance(), instance()) }
    bind<OperationsManager>() with provider { OperationsManager(instance(), instance()) }
}

val appModule = Kodein.Module("appModule") {
    bind<Int>("server-port") with singleton { System.getenv().getOrDefault("PORT", "8080").toInt() }
    bind<ObjectMapper>() with provider { jacksonObjectMapper() }
    bind<Boolean>("skipAuth") with singleton { System.getenv().getOrDefault("SKIP_AUTH", "true").toBoolean() }
    bind<PipelineController>() with provider { PipelineController(instance(), instance(), instance(), instance("skipAuth")) }
}

fun main(args: Array<String>) {
    val kodein = Kodein {
        import(DbModule.module)
        import(pipelineModule)
        import(appModule)
    }

    WebApplication(kodein).start()
}