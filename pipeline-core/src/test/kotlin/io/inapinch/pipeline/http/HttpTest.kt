package io.inapinch.pipeline.http

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class HttpTest {
    @Test
    fun testUrlWithParams_empty() {
        assertEquals("hi", Http.url("hi", emptyMap()))
    }

    @Test
    fun testUrlWithParams_single() {
        assertEquals("hi?hello=world", Http.url("hi", Collections.singletonMap("hello", "world")))
    }

    @Test
    fun testUrlWithParams_double() {
        assertEquals("hi?hello=world&meaningOfLife=42", Http.url("hi", mapOf(Pair("hello", "world"), Pair("meaningOfLife", 42))))
    }
}