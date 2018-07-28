package io.inapinch.pipeline

import io.inapinch.db.PipelineDao
import io.inapinch.pipeline.operations.*
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
                .thenApplyAsync { apply(it) }
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

        fun apply(request: PipelineRequest) : Any {
            var pipeline : Pipeline<out Any> = Pipeline.from(request.start.value)
            for(operation in request.operations)
                pipeline = when(operation) {
                    is Group -> pipeline.map { (it as Collection<Any>).toMutableList().chunked(operation.count) }
                    is Skip -> pipeline.skip(operation.count)
                    else -> pipeline.map {  (operation as AnyOperation).invoke(it)  }
                }
            return pipeline.result().get()
        }
    }
}

data class PipelineRequest(val start: Identity<out Any>,
                           val operations : List<Operation<out Any, out Any>> = listOf(),
                           val destination : Destination? = null,
                           val binding : Map<String, Any> = mapOf())

data class PipelineStatus(val message: String, val result: Any? = null)