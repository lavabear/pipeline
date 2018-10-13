package io.inapinch.pipeline.operations

import io.inapinch.pipeline.http.Http

class GetHtml : FunctionalOperation<String, String>(Http::get)

data class CreateUrl(val baseUrl: String) : FunctionalOperation<Map<String, Any>, String>({ Http.url(baseUrl, it) })

class GetJson : FunctionalOperation<String, Map<String, Any>>(Http::getJson)