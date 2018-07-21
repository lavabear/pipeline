package io.inapinch.pipeline

import kotlin.reflect.KClass

typealias CombinationEntry = Pair<CombinationPair, Combiner>

typealias CombinationPair = Pair<KClass<*>, KClass<*>>

typealias Combiner = (Any, Any) -> Any

class CombinationsManager private constructor(){
    companion object {
        private val combinations: Map<CombinationPair, Combiner> = combiners()

        fun <S : Any, T : Any> combine(first: S, second: T) : Any {
            return combinations[CombinationPair(first::class, second::class)]?.invoke(first, second)
                    ?: throw IllegalArgumentException("Bad request no way to combine $first and $second")
        }

        fun <S : Any, T : Any> valid(first: S, second: T) : Boolean = combinations.containsKey(CombinationPair(first::class, second::class))
    }
}

private fun pair(combiner: Combiner, first: KClass<*>, second: KClass<*> = first): CombinationEntry = Pair(CombinationPair(first, second), combiner)

private fun plusPairs(vararg clazzes: KClass<out Number>) : Array<CombinationEntry> {
    val result = mutableListOf<Pair<CombinationPair, Combiner>>()

    for (clazz: KClass<out Number> in clazzes)
        for (clazz2: KClass<out Number> in clazzes)
            result.add(pair({ a, b -> plus(a, b as Number)}, clazz, clazz2))
    return result.toTypedArray()
}

private fun plus(a: Any, other: Number) : Number = when (a) {
        is Long   -> a.toLong() + other.toLong()
        is Int    -> a.toInt()  + other.toInt()
        is Short  -> a.toShort() + other.toShort()
        is Byte   -> a.toByte() + other.toByte()
        is Double -> a.toDouble() + other.toDouble()
        is Float  -> a.toFloat() + other.toFloat()
        else      -> throw RuntimeException("Unknown numeric type")
    }

private fun  <S : Any> reflectionPair(name: String, first: KClass<S>) : CombinationEntry = reflectionPair(name, first, first)

private fun  <S : Any, T : Any> reflectionPair(name: String, first: KClass<S>, second: KClass<T>) : CombinationEntry = pair(reflectionCombiner(name, first, second), first, second)

private fun <S : Any, T : Any> reflectionCombiner(methodName: String, first: KClass<S>, second: KClass<T>) : Combiner =
        { a, b -> first.java.getMethod(methodName, second.java).invoke(a, b) }

private fun combiners() : Map<CombinationPair, Combiner> {
    val combineFunctional: Combiner = { a, b -> FunctionalOperation { CombinationsManager.combine((a as Operation).value(), (b as Operation).value()) }}
    return mapOf(
            reflectionPair("concat", String::class),

            *plusPairs(Int::class, Long::class, Float::class, Short::class, Integer::class, Double::class),

            pair({ a, b -> IdentityOperation(CombinationsManager.combine((a as Operation).value(), (b as Operation).value()))}, IdentityOperation::class),

            pair(combineFunctional, FunctionalOperation::class),
            pair(combineFunctional, FunctionalOperation::class, IdentityOperation::class),
            pair(combineFunctional, IdentityOperation::class, FunctionalOperation::class)
    )
}