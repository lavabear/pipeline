package io.inapinch.pipeline.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request

interface HttpClient {
    fun objectMapper(): ObjectMapper

    fun get(url: String) : String

    fun getJson(url: String) : Map<String, Any> = objectMapper().readValue(get(url))
}

private class OkHttp(private val okhttp: OkHttpClient = OkHttpClient.Builder().build(),
                     private val objectMapper: ObjectMapper = jacksonObjectMapper()) : HttpClient {

    override fun objectMapper(): ObjectMapper = objectMapper

    override fun get(url: String) : String {
        return okhttp.newCall(Request.Builder().url(url).get().build())
                .execute()
                .body()!!
                .string()
    }
}

object Http{
    private val client : HttpClient = OkHttp()

    fun get(url: String) : String = client.get(url)

    fun getJson(url: String) : Map<String, Any> = client.getJson(url)

    fun url(baseUrl: String, params: Map<String, Any>) : String =
            "$baseUrl${params.entries.stream()
                    .map{ "${it.key}=${it.value}" }
                    .reduce("", { key, value ->  "$key&$value" }, { t, _ -> t })
                    .replaceFirst('&', '?')}"
}