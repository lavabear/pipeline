package io.inapinch.pipeline.db

import org.jetbrains.exposed.sql.Table

object AuthTokens : Table() {
    val token = text("token").uniqueIndex().primaryKey()
    val created = datetime("created").index()
}

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

object ScheduledItems : Table() {
    val uuid = uuid("uuid").primaryKey()
    val cron = text("cron").index()
    val requestId = uuid("request_id").index()
    val created = datetime("created").index()
}