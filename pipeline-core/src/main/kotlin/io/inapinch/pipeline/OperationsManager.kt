package io.inapinch.pipeline

import io.inapinch.pipeline.db.PipelineDao
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class OperationsManager(private val pipelineDao: PipelineDao, private val resClient: DestinationClient) {

    private val inProgress: MutableMap<String, CompletableFuture<*>> = ConcurrentHashMap()

    fun enqueue(request: PipelineRequest) : String {
        val uuid = UUID.randomUUID().toString()
        inProgress[uuid] = CompletableFuture.completedFuture(request)
                .thenApplyAsync { pipelineDao.saveRequest(uuid, it); it }
                .thenApplyAsync(PipelineRequest::apply)
                .thenApplyAsync { pipelineDao.saveResult(uuid, it); it }
                .thenApplyAsync {
                    if(request.destination != null)
                        resClient.send(request.destination, it)
                    it }
                .exceptionally {
                    val message = "Failed to process pipeline: $uuid\n${it.localizedMessage}"
                    LOG.error(message, it)
                    message
                }
        return uuid
    }

    fun status(uuid: String) : PipelineStatus {
        val future = inProgress[uuid]
        if(future == null) {
            val result = pipelineDao.result(uuid)
            if(result.isPresent)
                return PipelineStatus("Found", result.get())
            return PipelineStatus("Not Found", uuid)
        }
        else if(!future.isDone)
            return PipelineStatus("In Progress", uuid)
        return PipelineStatus("Finished", future.get())
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OperationsManager::class.java)
    }
}

data class PipelineStatus(val message: String, val result: Any? = null)