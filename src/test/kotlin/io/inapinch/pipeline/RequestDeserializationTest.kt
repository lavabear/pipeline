package io.inapinch.pipeline

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class RequestDeserializationTest {

    @Test
    fun testBasicRequest() {
        val request = """
            {
                "start":
                    {
                        "@type": "Identity",
                        "value": 4
                    }
            }
        """.trimIndent()
        val objectMapper = jacksonObjectMapper()
        val pipelineRequest = objectMapper.readValue(request, PipelineRequest::class.java)
        val expected = PipelineRequest(Identity(4))
        assertEquals(expected, pipelineRequest)
    }

    @Test
    fun testFullRequest() {
        val request = """
            {
            "start": {
                        "@type": "Identity",
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
        val objectMapper = jacksonObjectMapper()
        val pipelineRequest = objectMapper.readValue(request, PipelineRequest::class.java)
        val expected = "Heo"
        assertEquals(expected, OperationsManager.apply(pipelineRequest))
    }
}