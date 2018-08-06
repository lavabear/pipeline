package io.inapinch.pipeline

import io.inapinch.pipeline.operations.Start
import io.inapinch.pipeline.operations.Reduce
import io.inapinch.pipeline.operations.RegexReplace
import io.inapinch.pipeline.operations.RegexSplit
import org.junit.Test
import kotlin.test.assertEquals

class OperationsManagerTest {

    @Test
    fun testPipelineRequest_basic() {
        assertEquals("Hugs", OperationsManager.apply(PipelineRequest(Start("Hugs"))))
    }

    @Test
    fun testPipelineRequest_regexReplace() {
        assertEquals("", OperationsManager.apply(PipelineRequest(Start("HAHA"), listOf(
                RegexReplace("H", ""),
                RegexReplace("A", "")))))
    }

    @Test
    fun testPipelineRequest_regexSplit() {
        assertEquals(listOf("A", "A"), OperationsManager.apply(PipelineRequest(Start("HAHA"), listOf(RegexSplit("H")))))
    }

    @Test
    fun testPipelineRequest_regexSplitReduce() {
        assertEquals("AA", OperationsManager.apply(PipelineRequest(Start("HAHA"),
                listOf(RegexSplit("H"), Reduce()))))
    }
}