package io.inapinch.pipeline.operations

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.inapinch.pipeline.db.PipelineDao
import io.inapinch.pipeline.CombinationsHandler
import io.inapinch.pipeline.PipelineRequest
import java.util.*

@FunctionalInterface
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
@JsonSubTypes(Type(Start::class), Type(Reduce::class), Type(Group::class), Type(ToInt::class),
        Type(GetHtml::class), Type(GetJson::class), Type(Object::class), Type(Apply::class), Type(Remove::class),
        Type(CreateDate::class), Type(Skip::class), Type(ToDateTime::class), Type(Add::class),
        Type(HtmlSelect::class), Type(Run::class), Type(CreateUrl::class),
        Type(ApplyKeys::class), Type(RegexReplace::class), Type(ToString::class), Type(Combine::class),
        Type(RegexSplit::class))
interface Operation<T, R> {
    fun invoke(input: T) : R

    fun combine(other: Operation<T, R>) : Operation<T, R> = CombinationsHandler.combine(this, other) as Operation<T, R>
}

typealias MapOperation = FunctionalOperation<Map<String, Any>, Map<String, Any>>
typealias AnyOperation = Operation<Any, Any>

open class FunctionalOperation<T, R>(@JsonIgnore private val value: (T) -> R)
    : Operation<T, R> { override fun invoke(input: T) : R = value.invoke(input) }

data class Start<T : Any>(@JsonProperty("value") val value: T) : Operation<T, T> {
    override fun invoke(input: T): T = value
}

class Run : Operation<UUID, PipelineRequest> {
    override fun invoke(input: UUID): PipelineRequest = PipelineDao.instance.request(input)
            .orElse(PipelineRequest(start = Start("$input does not exist")))
}