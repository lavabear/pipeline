package io.inapinch.pipeline

import io.inapinch.pipeline.operations.FunctionalOperation
import io.inapinch.pipeline.operations.Start
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
        assertEquals("Hugs!", Start("Hu")
                .combine(Start("gs!"))
                .invoke("Hello")) // Start ignores input
    }

    @Test
    fun testCombinationsManager_functional() {
        assertEquals("Hugs!", Start("Hu")
                .combine(FunctionalOperation { t -> t + "gs!" })
                .invoke(""))

        assertEquals("Hugs!", FunctionalOperation { _: String -> "H" }
                .combine(FunctionalOperation { t: String -> t + "u" })
                .combine(FunctionalOperation { t: String -> t + "g" })
                .combine(FunctionalOperation { t: String -> t + "s" })
                .combine(FunctionalOperation { t: String -> "$t!" })
                .invoke(""))

        assertEquals("gs!", FunctionalOperation { _: String -> "Hu" }
                .combine(Start("gs!"))
                .invoke(""))
    }
}
