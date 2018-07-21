package io.inapinch.pipeline


typealias OperationsPipeline = Pipeline<Operation>

class OperationsManager {
    fun adapt(request: PipelineRequest) : OperationsPipeline = Pipeline(request.operations.stream())
}

data class PipelineRequest(val operations : List<Operation> = listOf(), val destination : Destination? = null)