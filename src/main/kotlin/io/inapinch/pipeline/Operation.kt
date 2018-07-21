package io.inapinch.pipeline

interface Operation {
    fun combine(other: Operation) : Operation = CombinationsManager.combine(this, other) as Operation

    fun value() : Any
}

class FunctionalOperation(val value: () -> Any) : Operation {

    override fun value() : Any = value.invoke()
}

class IdentityOperation<T : Any>(private val value: T) : Operation {

    override fun value() : Any = value
}