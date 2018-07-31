package io.inapinch.pipeline.operations

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.inapinch.db.PipelineDao
import io.inapinch.pipeline.CombinationsManager
import io.inapinch.pipeline.PipelineRequest

@FunctionalInterface
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@type")
@JsonSubTypes(Type(Identity::class), Type(Reduce::class), Type(Group::class), Type(ToInt::class),
        Type(GetHtml::class), Type(GetJson::class), Type(Object::class), Type(Apply::class), Type(Remove::class),
        Type(CreateDate::class), Type(Skip::class), Type(ToDateTime::class), Type(Add::class),
        Type(HtmlSelect::class), Type(Run::class),
        Type(ApplyKeys::class), Type(RegexReplace::class), Type(ToString::class), Type(Combine::class),
        Type(RegexSplit::class))
interface Operation<T, R> {
    fun invoke(input: T) : R

    fun combine(other: Operation<T, R>) : Operation<T, R> {
        return CombinationsManager.combine(this, other) as Operation<T, R>
    }
}

typealias AnyOperation = Operation<Any, Any>

open class FunctionalOperation<T, R>(@JsonIgnore private val value: (T) -> R)
    : Operation<T, R> { override fun invoke(input: T) : R = value.invoke(input) }

data class Identity<T : Any>(@JsonProperty("value") val value: T) : Operation<T, T> {
    override fun invoke(input: T): T = value
}

data class Run(@JsonProperty("id") val id: String) : Operation<String, PipelineRequest> {
    override fun invoke(input: String): PipelineRequest = PipelineDao.instance.request(input)
            .orElse(PipelineRequest(start = Identity("$input does not exist")))
}