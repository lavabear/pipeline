package io.inapinch.pipeline

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OperationsManagerTest {

    @Test
    fun testPipelineRequest_basic() {
        val manager = OperationsManager()
        assertFalse(manager.adapt(PipelineRequest()).result().isPresent)
        assertEquals("Hugs", manager.adapt(PipelineRequest(listOf(IdentityOperation("Hugs")))).result().get().value())
    }
}