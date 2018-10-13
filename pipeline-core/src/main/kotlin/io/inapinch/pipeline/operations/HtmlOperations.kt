package io.inapinch.pipeline.operations

import org.jsoup.Jsoup
import java.util.stream.Collectors

data class HtmlSelect(val selectors: Set<String> = setOf())
    : FunctionalOperation<String, Map<String, List<String>>>({
    val document = Jsoup.parse(it)
    selectors.stream()
            .map { Pair(it, document.body()
                    .select(it)
                    .flatMap { it.allElements.map { it.text() } })
            }.collect(Collectors.toMap({it.first}, {it.second}))
})