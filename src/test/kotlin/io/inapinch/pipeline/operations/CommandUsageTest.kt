package io.inapinch.pipeline.operations

import org.junit.Test
import kotlin.test.assertEquals

class CommandUsageTest {
    @Test
    fun testCommandUsageForStart()
    {
        val actual = CommandUsage.commandUsage(Start::class)
        val expected = CommandUsage("Start",
                arguments = listOf(CommandArgument("value", DataType.of(DataType.ANY))),
                inputType = DataType.of(DataType.NONE),
                outputType = DataType.of(DataType.ANY))
        assertEquals(expected, actual)
    }

    @Test
    fun testCommandUsageForRun()
    {
        val actual = CommandUsage.commandUsage(Run::class)
        val expected = CommandUsage("Run",
                inputType = DataType.of(DataType.UUID),
                outputType = DataType.of(DataType.ANY))
        assertEquals(expected, actual)
    }

    @Test
    fun testCommandUsageForGetHtml()
    {
        val actual = CommandUsage.commandUsage(GetHtml::class)
        val expected = CommandUsage("GetHtml",
                inputType = DataType.of(DataType.URL),
                outputType =  DataType.of(DataType.STRING))
        assertEquals(expected, actual)
    }

    @Test
    fun testCommandUsageForGetJson()
    {
        val actual = CommandUsage.commandUsage(GetJson::class)
        val expected = CommandUsage("GetJson",
                inputType = DataType.of(DataType.URL),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, actual)
    }

    @Test
    fun testCommandUsageForCreateDate()
    {
        val actual = CommandUsage.commandUsage(CreateDate::class)
        val expected = CommandUsage("CreateDate",
                arguments = listOf(CommandArgument("keys", DataType.of(DataType.LIST, DataType.STRING)),
                    CommandArgument("key", DataType.of(DataType.STRING)),
                    CommandArgument("format", DataType.of(DataType.STRING)),
                    CommandArgument("remove", DataType.of(DataType.BOOL), required = false)),
                inputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, actual)
    }

    @Test
    fun testCommandUsageForApplyKeys()
    {
        val actual = CommandUsage.commandUsage(ApplyKeys::class)
        val expected = CommandUsage("ApplyKeys",
                arguments = listOf(CommandArgument("keys", DataType.of(DataType.SET, DataType.STRING)),
                        CommandArgument("operation", DataType.of(DataType.OPERATION))),
                inputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, actual)
    }

    @Test
    fun testCommandUsageForAdd()
    {
        val actual = CommandUsage.commandUsage(Add::class)
        val expected = CommandUsage("Add",
                arguments = listOf(CommandArgument("entries", DataType.of(DataType.SET, DataType.ENTRY))),
                inputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, actual)
    }
}