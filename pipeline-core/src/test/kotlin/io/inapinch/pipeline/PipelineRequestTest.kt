package io.inapinch.pipeline

import io.inapinch.pipeline.operations.Start
import io.inapinch.pipeline.operations.Reduce
import io.inapinch.pipeline.operations.RegexReplace
import io.inapinch.pipeline.operations.RegexSplit
import org.junit.Test
import kotlin.test.assertEquals

class PipelineRequestTest {

    @Test
    fun testBasic() {
        val request = PipelineRequest(Start("Hugs"))
        assertEquals("Hugs", request.apply())
    }

    @Test
    fun testRegexReplace() {
        val request = PipelineRequest(Start("HAHA"), listOf(RegexReplace("H", "")))
        assertEquals("AA", request.apply())
    }

    @Test
    fun testRegexSplit() {
        val request = PipelineRequest(Start("HAHA"), listOf(RegexSplit("H")))
        assertEquals(listOf("A", "A"), request.apply())
    }

    @Test
    fun testReduce() {
        val request = PipelineRequest(Start(listOf("A", "A")), listOf(Reduce()))
        assertEquals("AA", request.apply())
    }
}