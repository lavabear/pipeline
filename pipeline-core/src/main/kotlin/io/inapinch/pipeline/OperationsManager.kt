package io.inapinch.pipeline

import io.inapinch.pipeline.db.PipelineDao
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class OperationsManager(private val pipelineDao: PipelineDao, private val resClient: DestinationClient) {

    private val inProgress: MutableMap<UUID, CompletableFuture<*>> = ConcurrentHashMap()

    fun enqueue(request: PipelineRequest) : UUID {
        val uuid = UUID.randomUUID()
        inProgress[uuid] = CompletableFuture.completedFuture(request)
                .thenApplyAsync { pipelineDao.saveRequest(uuid, it); it }
                .thenApplyAsync(PipelineRequest::apply)
                .thenApplyAsync { pipelineDao.saveResult(UUID.randomUUID(), it, uuid); it }
                .thenApplyAsync {
                    if(request.destination != null)
                        resClient.send(request.destination, it)
                    it }
                .thenApplyAsync { inProgress.remove(uuid); it }
                .exceptionally {
                    val message = "Failed to process pipeline: $uuid\n${it.localizedMessage}"
                    LOG.error(message, it)
                    message
                }
        return uuid
    }

    fun status(uuid: UUID) : PipelineStatus {
        val future = inProgress[uuid]
        return if(future == null) {
            val result = pipelineDao.result(uuid)
            if(result.isPresent)
                PipelineStatus("Found", result.get())
             PipelineStatus("Not Found", uuid)
        }
        else if(!future.isDone)
            PipelineStatus("In Progress", uuid)
        else PipelineStatus("Finished", future.get())
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OperationsManager::class.java)
    }
}

data class PipelineStatus(val message: String, val result: Any? = null)