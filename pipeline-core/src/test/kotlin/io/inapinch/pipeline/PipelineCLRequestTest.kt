package io.inapinch.pipeline

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.inapinch.pipeline.operations.Group
import io.inapinch.pipeline.operations.Object
import io.inapinch.pipeline.operations.Start
import org.junit.Test
import kotlin.test.assertEquals

class PipelineCLRequestTest {

    @Test
    fun testToPipelineRequest_simple() {
        assertEquals(PipelineRequest(Start(42)), PipelineCLRequest("42").toPipelineRequest(jacksonObjectMapper()))
    }

    @Test
    fun testToPipelineRequest_oneOperation() {
        assertEquals(PipelineRequest(Start(42), operations = listOf(Group(1))), PipelineCLRequest("42 => group count: 1").toPipelineRequest(jacksonObjectMapper()))
    }

    @Test
    fun testToPipelineRequest_twoOperations() {
        val expected = PipelineRequest(Start(42), operations = listOf(Group(1), Object(listOf("meaningOfLife"))))
        assertEquals(expected, PipelineCLRequest("42 => group count: 1 => object keys: [\"meaningOfLife\"]").toPipelineRequest(jacksonObjectMapper()))
    }
}