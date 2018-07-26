package io.inapinch.pipeline

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

interface Pipeline<T : Any> {

    fun <R: Any> map(operation: (T) -> R): Pipeline<R>

    fun <R: Any> flatMap(operation: (T) -> Stream<R>) : Pipeline<R>

    fun filter(operation: (T) -> Boolean): Pipeline<T>

    fun toList() : Collection<T>

    fun skip(skip : Int = 1) : Pipeline<T>

    fun result() : Optional<T>

    companion object {
        fun <T: Any> from(items: Collection<T>) : Pipeline<T> = SinglePipeline(items.stream())
        fun <T: Any> from(vararg items: T) : Pipeline<T> = SinglePipeline(Arrays.stream(items))
    }
}

private class SinglePipeline<T : Any> constructor(private val items: Stream<T>) : Pipeline<T> {

    override fun <R: Any> map(operation: (T) -> R): Pipeline<R> = SinglePipeline(items.map(operation::invoke))

    override fun <R: Any> flatMap(operation: (T) -> Stream<R>): Pipeline<R> = SinglePipeline(items.flatMap(operation::invoke))

    override fun filter(operation: (T) -> Boolean): Pipeline<T> = SinglePipeline(items.filter(operation::invoke))

    override fun toList() : Collection<T> = items.collect(Collectors.toList())

    override fun skip(skip : Int) : Pipeline<T> = SinglePipeline(items.skip(skip.toLong()))

    override fun result() : Optional<T> = items.reduce { first, second -> CombinationsManager.combine(first, second) as T }
}