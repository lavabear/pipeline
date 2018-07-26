package io.inapinch.pipeline

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PipelineTest {

    @Test
    fun testPipeline() {
        assertEquals(listOf("H", "u", "g", "s", "!"), Pipeline.from("Hey you guys!")
                .map { it.replace(Regex("[eouy]+"), "")}
                .map { it.replace(Regex(" +"), "u")}
                .flatMap { it.split("").stream() }
                .filter(String::isNotEmpty)
                .toList())
    }

    @Test
    fun testPipeline_duplicate() {
        val expected = listOf(listOf("H", "u", "g", "s", "!"), listOf("H", "u", "g", "s", "!"))
        assertEquals(expected, Pipeline.from("Hey you guys!").duplicate()
                .map { it.replace(Regex("[eouy]+"), "")}
                .map { it.replace(Regex(" +"), "u")}
                .flatMap { it.split("").stream() }
                .filter(String::isNotEmpty)
                .toList())

        assertEquals(listOf("Hugs!", "Hugs!"), Pipeline.from("Hey you guys!").duplicate()
                .map { it.replace(Regex("[eouy]+"), "")}
                .map { it.replace(Regex(" +"), "u")}
                .flatMap { it.split("").stream() }
                .filter(String::isNotEmpty)
                .result())
    }

    @Test
    fun testCombinationsManager_basic() {
        assertEquals("Hugs!", CombinationsManager.combine("Hu", "gs!"))
        assertEquals(3, CombinationsManager.combine(1, 2))
        assertEquals(1.5, CombinationsManager.combine(1.5, 0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCombinationsManager_badRequest() {
        assertFalse(CombinationsManager.valid(List::class, String::class))
        CombinationsManager.combine(listOf(1), "gs!")
    }

    @Test
    fun testCombinationsManager_identityOperations() {
        assertEquals("Hugs!", Identity("Hu")
                .combine(Identity("gs!"))
                .invoke("Hello")) // Identity ignores input
    }

    @Test
    fun testCombinationsManager_functional() {
        assertEquals("Hugs!", Identity("Hu")
                .combine(FunctionalOperation { t ->  t+"gs!" })
                .invoke(""))

        assertEquals("Hugs!", FunctionalOperation {_: String -> "H"}
                .combine(FunctionalOperation {t: String -> t+"u"})
                .combine(FunctionalOperation {t: String -> t+"g"})
                .combine(FunctionalOperation { t: String -> t+"s" })
                .combine(FunctionalOperation { t: String -> "$t!" })
                .invoke(""))

        assertEquals("gs!", FunctionalOperation {_: String -> "Hu"}
                .combine(Identity ("gs!" ))
                .invoke(""))
    }
}
