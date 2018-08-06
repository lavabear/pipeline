package io.inapinch.pipeline

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.inapinch.pipeline.operations.Start
import org.junit.Test
import kotlin.test.assertEquals

val objectMapper = jacksonObjectMapper()

class RequestDeserializationTest {

    @Test
    fun testBasicRequest() {
        val request = """
            {
                "start":
                    {
                        "@type": "Start",
                        "value": 4
                    }
            }
        """.trimIndent()
        val pipelineRequest = objectMapper.readValue(request, PipelineRequest::class.java)
        val expected = PipelineRequest(Start(4))
        assertEquals(expected, pipelineRequest)
    }

    @Test
    fun testFullRequest() {
        val request = """
            {
            "start": {
                        "@type": "Start",
                        "value": "Hello"
                    },
            "operations": [{
                        "@type": "RegexSplit",
                        "split": "l"
                    },
                    {
                        "@type": "Reduce"
                    }
                ]
            }
        """.trimIndent()
        val pipelineRequest = objectMapper.readValue(request, PipelineRequest::class.java)
        val expected = "Heo"
        assertEquals(expected, OperationsManager.apply(pipelineRequest))
    }
}