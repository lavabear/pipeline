package io.inapinch.pipeline.operations

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.streams.toList

data class CreateDate(val keys: List<String> = listOf(), val key: String,
                      val format: String, val remove: Boolean = false)
    :  FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    val map = it.toMutableMap()
    map[key] = LocalDate.parse(keys.stream()
            .map { k -> it[k] as String }.toList()
            .joinToString(" "), DateTimeFormatter.ofPattern(format))
    if(remove)
        keys.forEach { map.remove(it)}
    map
})

data class RegexSplit(val split: String, val limit: Int = 0, val skip: Int = 0)
    : FunctionalOperation<String, List<String>>({ t ->
    t.split(split.toRegex(), limit)
            .stream().filter { it.isNotEmpty() }.skip(skip.toLong()).toList()
})

data class RegexReplace(val find: String, val replace: String)
    : FunctionalOperation<String, String>({ t -> t.replace(find.toRegex(), replace)})
