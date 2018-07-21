package io.inapinch.pipeline


interface DestinationClient {
    fun send(destination: Destination, it: Any)
}

class GraphqlDestinationClient : DestinationClient {
    override fun send(destination: Destination, it: Any) {

    }
}

class RestDestinationClient : DestinationClient {
    override fun send(destination: Destination, it: Any) {

    }
}

enum class DestinationType { REST, GRAPHQL }
data class Destination(val url: String, val type: DestinationType)