package io.inapinch.pipeline.operations

import com.fasterxml.jackson.annotation.JsonSubTypes
import kotlin.reflect.KClass
import kotlin.reflect.KType

data class CommandUsage(val command: String,
                        val arguments: List<CommandArgument> = listOf(),
                        val inputType: List<DataType>,
                        val outputType: List<DataType>) {
    companion object {
        fun all(): List<CommandUsage>  = Operation::class.java
                .getAnnotation(JsonSubTypes::class.java)
                .value.map { commandUsage(it.value) }

        fun commandUsage(type: KClass<*>): CommandUsage {
            val command = type.simpleName!!
            val parentTypes = type.supertypes

            return CommandUsage(
                    command = command,
                    inputType = inputType(command, parentTypes),
                    outputType = outputType(command, parentTypes),
                    arguments = if(type.constructors.isEmpty()) listOf() else CommandArgument.from(type.constructors.first()))
        }
    }
}

private fun inputType(command: String, parentTypes: List<KType>) : List<DataType> = when(command)
{
    "Start" ->  DataType.of(DataType.NONE)
    "Run" ->  DataType.of(DataType.UUID)
    "GetHtml" ->  DataType.of(DataType.URL)
    "GetJson" ->  DataType.of(DataType.URL)
    else -> {
        if(parentTypes.size == 1)
            DataType.from(parentTypes.first().arguments[0].type!!)
        else  DataType.of(DataType.ANY)
    }
}

private fun outputType(command: String, parentTypes: List<KType>) : List<DataType> = when(command)
{
    "Run" -> DataType.of(DataType.ANY)
    "Start" ->  DataType.of(DataType.ANY)
    else -> {
        if(parentTypes.size == 1)
            DataType.from(parentTypes.first().arguments[1].type!!)
        else DataType.of(DataType.ANY)
    }
}