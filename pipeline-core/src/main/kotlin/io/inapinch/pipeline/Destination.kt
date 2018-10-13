package io.inapinch.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

@FunctionalInterface
interface DestinationClient {
    fun send(destination: Destination, it: Any)

    companion object {
        fun restClient(objectMapper: ObjectMapper, client: OkHttpClient) : DestinationClient = RestDestinationClient(objectMapper, client)
    }
}

private class RestDestinationClient(private val objectMapper: ObjectMapper,
                                    private val client: OkHttpClient) : DestinationClient {
    override fun send(destination: Destination, it: Any) {
        client.newCall(Request.Builder()
                .url(destination.url)
                .post(RequestBody.create(MediaType.parse("application/json"),
                        objectMapper.writeValueAsString(it)))
                .build()).execute()
    }
}

data class Destination(val url: String)