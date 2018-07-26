package io.inapinch.pipeline

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.inapinch.http.Http
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import kotlin.streams.toList

@FunctionalInterface
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
@JsonSubTypes(Type(Identity::class), Type(Reduce::class), Type(Group::class), Type(ToInt::class),
        Type(GetHtml::class), Type(GetJson::class), Type(Object::class), Type(Apply::class),
        Type(CreateDate::class), Type(Skip::class),
        Type(RegexSplit::class), Type(RegexReplace::class), Type(ToString::class), Type(Combine::class))
interface Operation<T, R> {
    fun invoke(input: T) : R

    fun combine(other: Operation<T, R>) : Operation<T, R> = CombinationsManager.combine(this, other) as Operation<T, R>
}

typealias AnyOperation = Operation<Any, Any>

open class FunctionalOperation<T, R>(@JsonIgnore private val value: (T) -> R)
    : Operation<T, R> { override fun invoke(input: T) : R = value.invoke(input) }

data class Group(val count: Int) : Operation<Any, Any> {
    override fun invoke(input: Any): Any = input
}

data class Skip(val count: Int) : Operation<Any, Any> {
    override fun invoke(input: Any): Any = input
}

data class ToInt(val keys: Set<String> = setOf())
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({ it.entries.stream().collect(Collectors.toMap({ it.key }, { if(keys.contains(it.key)) (it.value as String).replace("\\D+".toRegex(), "").toInt() else it.value }))})

data class ToString(val keys: Set<String> = setOf())
    : FunctionalOperation<Map<String, Any>, Map<String, Any>>({ it.entries.stream().collect(Collectors.toMap({ it.key }, { if(keys.contains(it.key)) it.value.toString() else it.value }))})

data class Apply(val operation: Operation<Any, *>) :  FunctionalOperation<List<Any>, Any>({ it.map { operation.invoke(it)} })

data class Object(val keys: List<String> = listOf()) :  FunctionalOperation<List<Any>, Map<String, Any>>({ keys.zip(it).toMap() })

data class Combine(val keys: List<String> = listOf(), val key: String, val remove: Boolean = false) :  FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    val map = it.toMutableMap()
    map[key] = keys.stream().map { k -> it[k] }.toList()
    if(remove)
        keys.forEach { map.remove(it)}
    map })

data class CreateDate(val keys: List<String> = listOf(), val key: String, val format: String, val remove: Boolean = false) :  FunctionalOperation<Map<String, Any>, Map<String, Any>>({
    val map = it.toMutableMap()
    map[key] = LocalDate.parse(keys.stream()
            .map { k -> it[k] as String }.toList()
            .joinToString(" "), DateTimeFormatter.ofPattern(format))
    if(remove)
        keys.forEach { map.remove(it)}
    map })

class GetHtml : FunctionalOperation<String, String>(Http::get)

class GetJson : FunctionalOperation<String, Map<String, Any>>(Http::getJson)

data class RegexSplit(val split: String, val limit: Int = 0, val skip: Int = 0)
    : FunctionalOperation<String, List<String>>({ t -> t.split(split.toRegex(), limit).stream().filter { it.isNotEmpty() }.skip(skip.toLong()).toList()})

data class RegexReplace(val find: String, val replace: String)
    : FunctionalOperation<String, String>({ t -> t.replace(find.toRegex(), replace)})

class Reduce<T : Any>
    : FunctionalOperation<Collection<T>, T>({ t -> t.stream().reduce { a : T, b: T -> CombinationsManager.combine(a, b) as T}.get()})

data class Identity<T : Any>(@JsonProperty("value") val value: T) : Operation<T, T> {
    override fun invoke(input: T): T = value
}