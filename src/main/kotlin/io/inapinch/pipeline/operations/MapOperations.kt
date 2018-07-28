package io.inapinch.pipeline.operations

import java.time.LocalDate
import java.util.stream.Collectors
import java.util.stream.Stream

data class Add(val entries: Set<Entry> = setOf())
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    Stream.concat(entries.stream(), it.entries.stream().map { Entry(it.key, it.value) })
            .collect(Collectors.toMap({it.key}, {it.value}))
})

data class Remove(val keys: Set<String> = setOf())
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    it.entries.stream()
            .filter{ !keys.contains(it.key) }
            .collect(Collectors.toMap({it.key}, {it.value}))
})

data class ToInt(val keys: Set<String> = setOf())
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    it.entries
            .stream()
            .collect(Collectors.toMap({ it.key }, {
                if(keys.contains(it.key))
                    (it.value as String).replace("\\D+".toRegex(), "").toInt()
                else
                    it.value }))
})

data class ToDateTime(val keys: Set<String> = setOf(), val hour: Int = 0, val minute: Int = 0, val second: Int = 0 )
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    it.entries.stream()
            .collect(Collectors.toMap({ it.key },
                    { if(keys.contains(it.key))
                        (it.value as LocalDate).atTime(hour, minute, second)
                    else
                        it.value }))
})

data class ToString(val keys: Set<String> = setOf())
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    it.entries.stream()
            .collect(Collectors.toMap({ it.key },
                    { if(keys.contains(it.key)) it.value.toString() else it.value }))
})
