package io.inapinch.pipeline

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class Pipeline<T : Any> constructor(private val items: Stream<T>) {

    fun <R: Any> map(operation: (T) -> R): Pipeline<R> = Pipeline(items.map(operation::invoke))

    fun <R: Any> flatMap(operation: (T) -> Stream<R>): Pipeline<R> = Pipeline(items.flatMap(operation::invoke))

    fun filter(operation: (T) -> Boolean): Pipeline<T> = Pipeline(items.filter(operation::invoke))

    fun toList() : Collection<T> = items.collect(Collectors.toList())

    fun duplicate(copies : Int = 2) : ParallelPipelines<T> = copy(toList(), copies)

    fun fork(split : Int = 2) : ParallelPipelines<T> = parallel(toList(), split)

    fun result() : Optional<T> = items.reduce { first, second -> CombinationsManager.combine(first, second) as T }

    companion object {
        fun <T: Any> from(items: Collection<T>) : Pipeline<T> = Pipeline(items.stream())
        fun <T: Any> from(vararg items: T) : Pipeline<T> = Pipeline(Arrays.stream(items))

        fun <T : Any> copy(input: Collection<T>, copies: Int = 2) : ParallelPipelines<T> {
            val result = mutableListOf<Collection<T>>()
            for (i in 1..copies) {
                val new = mutableListOf<T>()
                Collections.copy(input.toMutableList(), new)
                result.add(new)
            }

            return ParallelPipelines(result.parallelStream()
                    .map { ArrayList(input).stream() })
        }

        fun <T : Any> parallel(input: Collection<T>, split: Int = 2) : ParallelPipelines<T> {
            return ParallelPipelines(input.toMutableList().chunked(split).stream().map { it.stream() })
        }
    }
}

class ParallelPipelines<T : Any>(val items: Stream<Stream<T>>) {
    fun <R: Any> map(operation: (T) -> R): ParallelPipelines<R> = ParallelPipelines(items.map { it.map(operation::invoke) })

    fun <R: Any> flatMap(operation: (T) -> Stream<R>): ParallelPipelines<R> = ParallelPipelines(items.map {  it.flatMap(operation::invoke) })

    fun filter(operation: (T) -> Boolean): ParallelPipelines<T> = ParallelPipelines(items.map { it.filter(operation::invoke) })

    fun toList() : Collection<Collection<T>> = items.map{ it.collect(Collectors.toList()) }.collect(Collectors.toList())

    fun fork(split : Int = 2) : ParallelPipelines<Collection<T>> = Pipeline.parallel(toList(), split)

    fun result() : List<T> = items.map { it.reduce { first, second -> CombinationsManager.combine(first, second) as T}.get() }.collect(Collectors.toList())
}