package io.inapinch.pipeline

import io.inapinch.pipeline.operations.*
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

private fun combinationPair(combiner: Combiner, first: KClass<*>, second: KClass<*> = first): CombinationEntry = Pair(CombinationPair(first, second), combiner)

private typealias NumberClass = KClass<out Number>

private fun plusPairs(vararg classes: NumberClass) : Array<CombinationEntry> {
    val result = mutableListOf<CombinationEntry>()

    for (clazz: NumberClass in classes)
        for (clazz2: NumberClass in classes)
            result.add(combinationPair({ a, b -> plus(a, b as Number)}, clazz, clazz2))
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

private fun  <S : Any, T : Any> reflectionPair(name: String, first: KClass<S>, second: KClass<T>) : CombinationEntry = combinationPair(reflectionCombiner(name, first, second), first, second)

private fun <S : Any, T : Any> reflectionCombiner(methodName: String, first: KClass<S>, second: KClass<T>) : Combiner =
        { a, b -> first.java.getMethod(methodName, second.java).invoke(a, b) }

private fun anyOperation(any: Any) : AnyOperation = any as AnyOperation

private fun combiners() : Map<CombinationPair, Combiner> {
    val combineFunctional: Combiner = { a, b -> FunctionalOperation { t: Any -> (anyOperation(b).invoke(anyOperation(a).invoke(t))) } }
    return mapOf(
            reflectionPair("concat", String::class),

            *plusPairs(Int::class, Long::class, Float::class, Short::class, Integer::class, Double::class),

            combinationPair({ a, b -> Start(CombinationsManager.combine(anyOperation(a).invoke(Any()), anyOperation(b).invoke(Any()))) }, Start::class),

            combinationPair(combineFunctional, FunctionalOperation::class),
            combinationPair(combineFunctional, FunctionalOperation::class, Start::class),
            combinationPair(combineFunctional, Start::class, FunctionalOperation::class),

            combinationPair(combineFunctional, Start::class, RegexReplace::class),
            combinationPair(combineFunctional, FunctionalOperation::class, RegexReplace::class),

            combinationPair(combineFunctional, Start::class, Reduce::class),
            combinationPair(combineFunctional, FunctionalOperation::class, Reduce::class),

            combinationPair(combineFunctional, Start::class, GetHtml::class),
            combinationPair(combineFunctional, FunctionalOperation::class, GetHtml::class),

            combinationPair(combineFunctional, Start::class, GetJson::class),
            combinationPair(combineFunctional, FunctionalOperation::class, GetJson::class),

            combinationPair(combineFunctional, Start::class, RegexSplit::class),
            combinationPair(combineFunctional, FunctionalOperation::class, RegexSplit::class)
    )
}