package io.inapinch.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.OkHttpClient
import okhttp3.Request

object Http {
    private val okhttp: OkHttpClient = OkHttpClient.Builder().build()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun get(url: String) : String = okhttp.newCall(Request.Builder().url(url).get().build()).execute().body()!!.string()
    fun getJson(url: String) : Map<String, Any> = objectMapper.readValue(get(url))
}