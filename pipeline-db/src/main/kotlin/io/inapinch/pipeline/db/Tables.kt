package io.inapinch.pipeline.db

import org.jetbrains.exposed.sql.Table

object VerifiedPipelineRequests : Table() {
    val uuid = uuid("uuid").primaryKey()
    val request = text("request")
    val created = datetime("created").index()
}

object VerifiedPipelineResults : Table() {
    val uuid = uuid("uuid").primaryKey()
    val result = text("result")
    val requestId = uuid("request_id").primaryKey()
    val created = datetime("created").index()
}