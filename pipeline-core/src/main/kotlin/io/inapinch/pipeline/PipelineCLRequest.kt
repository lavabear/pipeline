package io.inapinch.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.inapinch.pipeline.operations.Operation
import io.inapinch.pipeline.operations.Start
import java.util.regex.Pattern

data class PipelineCLRequest(val cl: String,
                             val destination : Destination? = null,
                             val name: String? = null,
                             override val binding : Map<String, Any> = mapOf()) : HasBinding {

    fun toPipelineRequest(objectMapper: ObjectMapper) : PipelineRequest {
        val results = cl.split("=>")
        return PipelineRequest(start = Start(objectMapper.readValue(results.first()) as Any),
                name = name, binding = binding, destination = destination,
                operations = results.drop(1).map { createOperation(objectMapper, it) })
    }

    private fun createOperation(objectMapper: ObjectMapper, rawCommand: String) : Operation<out Any, out Any> {
        val commandParts = rawCommand.trim().split(Pattern.compile("\\s+"), 2)
        val opName = commandParts.first()
        val operationName = opName[0].toUpperCase() + opName.substring(1)

        return objectMapper.readValue((if(commandParts.size == 1)
                    """
                        {
                            "@type": "$operationName"
                        }
                    """
                else
                    """
                        {
                            "@type": "$operationName",
                            ${createArguments(commandParts.last())}
                        }
                    """).trimIndent())
    }

    private fun createArguments(rawArguments: String) : String {
        return rawArguments.trim().split(Pattern.compile("\\s+"), 2).joinToString("") {
            if(it.last() == ':')
                "\"${it.dropLast(1)}\":"
            else if((it.first() == '[' && it.last() == ']') || (it.first() == '{' && it.last() == '}'))
                "$it,"
            else
                "\"$it\","
        }.dropLast(1)
    }
}
