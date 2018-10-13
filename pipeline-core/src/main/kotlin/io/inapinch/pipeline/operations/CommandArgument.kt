package io.inapinch.pipeline.operations

import com.google.common.collect.Sets
import kotlin.reflect.KFunction

data class CommandArgument(val name: String, val type: List<DataType>, val required: Boolean = true) {
    companion object {
        private val requiredTypes = Sets.newEnumSet(listOf(DataType.MAP, DataType.LIST, DataType.SET), DataType::class.java)

        fun from(constructor: KFunction<Any>) : List<CommandArgument> = constructor.parameters
                .map {
                    val dataType = DataType.from(it.type)
                    if(requiredTypes.contains(dataType[0]))
                        CommandArgument(it.name!!, dataType)
                    else
                        CommandArgument(it.name!!, dataType, !it.isOptional) }
    }
}