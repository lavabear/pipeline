package io.inapinch.pipeline

import org.junit.Test
import kotlin.test.assertEquals

class OperationsManagerTest {

    @Test
    fun testPipelineRequest_basic() {
        assertEquals("Hugs", OperationsManager.apply(PipelineRequest(Identity("Hugs"))))
    }

    @Test
    fun testPipelineRequest_regexReplace() {
        assertEquals("", OperationsManager.apply(PipelineRequest(Identity("HAHA"), listOf(
                RegexReplace("H", ""),
                RegexReplace("A", "")))))
    }

    @Test
    fun testPipelineRequest_regexSplit() {
        assertEquals(listOf("A", "A"), OperationsManager.apply(PipelineRequest(Identity("HAHA"), listOf(RegexSplit("H")))))
    }

    @Test
    fun testPipelineRequest_regexSplitReduce() {
        assertEquals("AA", OperationsManager.apply(PipelineRequest(Identity("HAHA"),
                listOf(RegexSplit("H"), Reduce<String>()))))
    }
}