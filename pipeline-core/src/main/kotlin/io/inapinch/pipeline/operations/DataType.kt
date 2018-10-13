package io.inapinch.pipeline.operations

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

enum class DataType {
    STRING, UUID, INT, LIST, MAP, ANY, NONE, DATE, DATE_TIME, BOOL, URL, OPERATION, SET, ENTRY;

    companion object {
        fun of(vararg types: DataType) : List<DataType> = types.toList()

        fun from(type: KType?): List<DataType> {
            if(type == null)
                return Collections.emptyList()
            val classifier = type.classifier
            return when(classifier) {
                is KClass<*> -> {
                    when(classifier.simpleName) {
                        "String" -> of(DataType.STRING)
                        "List" -> of(DataType.LIST, *from(type.arguments.first().type).toTypedArray())
                        "Set" -> of(DataType.SET, *from(type.arguments.first().type).toTypedArray())
                        "Map" -> of(DataType.MAP, *type.arguments.flatMap { from(it.type) }.toTypedArray())
                        "Operation" -> of(DataType.OPERATION)
                        "Entry" -> of(DataType.ENTRY)
                        "PipelineRequest" -> of(DataType.ANY)
                        "Int" -> of(DataType.INT)
                        "Boolean" -> of(DataType.BOOL)
                        "UUID" -> of(DataType.UUID)
                        else -> of(DataType.ANY)
                    }
                }
                else -> of(DataType.ANY)
            }
        }
    }
}
