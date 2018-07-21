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
        assertEquals("Hugs!", IdentityOperation("Hu")
                .combine(IdentityOperation("gs!"))
                .value())
    }

    @Test
    fun testCombinationsManager_functional() {
        assertEquals("Hugs!", IdentityOperation("Hu")
                .combine(FunctionalOperation { "gs!" })
                .value())
        assertEquals("Hugs!", FunctionalOperation {"Hu"}
                .combine(IdentityOperation ("gs!" ))
                .value())
        assertEquals("Hugs!", FunctionalOperation {"H"}
                .combine(FunctionalOperation {"u"})
                .combine(FunctionalOperation {"g"})
                .combine(FunctionalOperation { "s" })
                .combine(FunctionalOperation { "!" })
                .value())
    }
}
