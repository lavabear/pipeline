package io.inapinch.pipeline

import io.inapinch.pipeline.operations.*
import kotlin.reflect.KClass

private typealias CombinationEntry = Pair<KClassPair, Combiner>

private typealias Combiner = (Any, Any) -> Any

private typealias KClassPair = Pair<KClass<*>, KClass<*>>

class CombinationsHandler private constructor(){
    companion object {

        private val combineFunctional: Combiner = { a, b -> FunctionalOperation{
            t: Any -> (anyOperation(b).invoke(anyOperation(a).invoke(t)))
        } }

        private fun anyOperation(any: Any) : AnyOperation = any as AnyOperation

        private val combinations: Map<KClassPair, Combiner> = mapOf(
            reflectionPair("concat", String::class),

            *plusPairs(Int::class, Long::class, Float::class, Short::class, Integer::class, Double::class),

            combinationPair(combineFunctional, FunctionalOperation::class),
            combinationPair(combineFunctional, FunctionalOperation::class, Start::class),
            combinationPair(combineFunctional, Start::class, FunctionalOperation::class)
        )

        fun <S : Any, T : Any> combine(first: S, second: T) : Any {
            return combinations[KClassPair(first::class, second::class)]?.invoke(first, second)
                    ?: throw IllegalArgumentException("Bad request no way to combine $first and $second")
        }

        fun <S : Any, T : Any> valid(first: S, second: T) : Boolean {
            return combinations.containsKey(KClassPair(first::class, second::class))
        }

        private fun combinationPair(combiner: Combiner, first: KClass<*>, second: KClass<*> = first): CombinationEntry {
            return Pair(KClassPair(first, second), combiner)
        }

        private fun plusPairs(vararg classes: KClass<out Number>) : Array<CombinationEntry> {
            val result = mutableListOf<CombinationEntry>()

            for (clazz: KClass<out Number> in classes)
                for (clazz2: KClass<out Number> in classes)
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
                else      -> throw RuntimeException("Unknown numeric type: ${a.javaClass}")
            }

        private fun  <S : Any> reflectionPair(name: String, first: KClass<S>) : CombinationEntry {
            return reflectionPair(name, first, first)
        }

        private fun  <S : Any, T : Any> reflectionPair(name: String, first: KClass<S>, second: KClass<T>) : CombinationEntry {
            return combinationPair({ a, b -> first.java.getMethod(name, second.java).invoke(a, b) }, first, second)
        }
    }
}