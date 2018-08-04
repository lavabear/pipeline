package io.inapinch.pipeline.operations

import io.inapinch.pipeline.CombinationsManager
import java.util.stream.Collectors
import kotlin.streams.toList

data class Group(val count: Int) : Operation<Any, Any> {
    override fun invoke(input: Any): Any = input
}

data class Skip(val count: Int) : Operation<Any, Any> {
    override fun invoke(input: Any): Any = input
}

data class Object(val keys: List<String> = listOf()) : FunctionalOperation<List<Any>, Map<String, Any>>({
    keys.zip(it).toMap()
})

data class Entry(val key: String, val value: Any)

data class Apply(val operation: Operation<Any, *>)
    :  FunctionalOperation<List<Any>, List<Any>>({ it.map { operation.invoke(it)!!} })

data class ApplyKeys(val keys: Set<String> = setOf(), val operation: Operation<Any, *>) : MapOperation ({
    it.entries.stream()
            .map { Entry(it.key, if (keys.contains(it.key)) operation.invoke(it)!! else it) }
            .collect(Collectors.toMap({it.key}, {it.value}))
})

data class Combine(val keys: List<String> = listOf(), val key: String, val remove: Boolean = false) :  MapOperation ({
    val map = it.toMutableMap()
    map[key] = keys.stream().map { k -> it[k] }.toList()
    if(remove)
        keys.forEach { map.remove(it)}
    map
})

class Reduce : FunctionalOperation<Collection<Any>, Any>({ t ->
    t.stream().reduce { a : Any, b: Any -> CombinationsManager.combine(a, b)}.get()
})
