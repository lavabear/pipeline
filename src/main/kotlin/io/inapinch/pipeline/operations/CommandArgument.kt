package io.inapinch.pipeline.operations

import kotlin.reflect.KFunction

data class CommandArgument(val name: String, val type: List<DataType>, val required: Boolean = true) {
    companion object {
        fun from(constructor: KFunction<Any>) : List<CommandArgument> = constructor.parameters
                .map {
                    val dataType = DataType.from(it.type)
                    if(dataType[0] == DataType.LIST || dataType[0] == DataType.MAP)
                        CommandArgument(it.name!!, dataType)
                    else
                        CommandArgument(it.name!!, dataType, !it.isOptional) }
    }
}