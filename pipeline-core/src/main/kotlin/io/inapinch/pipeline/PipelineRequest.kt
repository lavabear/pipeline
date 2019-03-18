package io.inapinch.pipeline

import io.inapinch.pipeline.operations.*
import java.util.*

data class PipelineRequest(val start: Start<out Any>,
                           val operations : List<Operation<out Any, out Any>> = listOf(),
                           val destination : Destination? = null,
                           val name: String? = null,
                           override val binding : Map<String, Any> = mapOf()) : HasBinding {

    fun apply() : Any {
        var pipeline : Pipeline<out Any> = Pipeline.from(this.start.value)
        for(operation in this.operations)
            pipeline = when(operation) {
                is Group -> pipeline.map { (it as? Collection<*>)?.toMutableList()?.chunked(operation.count)
                        ?: Collections.singletonList(Collections.singletonList(it)) }
                is Skip -> pipeline.skip(operation.count)
                is Run -> pipeline.map { operation.invoke(UUID.fromString(it as String)).apply() }
                else -> pipeline.map((operation as AnyOperation)::invoke)
            }
        return pipeline.result().get()
    }
}
