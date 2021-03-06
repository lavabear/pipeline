package io.inapinch.pipeline.operations

import org.junit.Test
import kotlin.test.assertEquals

class CommandUsageTest {
    @Test
    fun testCommandUsageForStart()
    {
        val expected = CommandUsage("Start",
                arguments = listOf(CommandArgument("value", DataType.of(DataType.ANY))),
                inputType = DataType.of(DataType.NONE),
                outputType = DataType.of(DataType.ANY))
        assertEquals(expected, CommandUsage.commandUsage(Start::class))
    }

    @Test
    fun testCommandUsageForRun()
    {
        val expected = CommandUsage("Run",
                inputType = DataType.of(DataType.UUID),
                outputType = DataType.of(DataType.ANY))
        assertEquals(expected, CommandUsage.commandUsage(Run::class))
    }

    @Test
    fun testCommandUsageForGetHtml()
    {
        val expected = CommandUsage("GetHtml",
                inputType = DataType.of(DataType.URL),
                outputType =  DataType.of(DataType.STRING))
        assertEquals(expected, CommandUsage.commandUsage(GetHtml::class))
    }

    @Test
    fun testCommandUsageForGetJson()
    {
        val expected = CommandUsage("GetJson",
                inputType = DataType.of(DataType.URL),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, CommandUsage.commandUsage(GetJson::class))
    }

    @Test
    fun testCommandUsageForCreateDate()
    {
        val expected = CommandUsage("CreateDate",
                arguments = listOf(CommandArgument("keys", DataType.of(DataType.LIST, DataType.STRING)),
                    CommandArgument("key", DataType.of(DataType.STRING)),
                    CommandArgument("format", DataType.of(DataType.STRING)),
                    CommandArgument("remove", DataType.of(DataType.BOOL), required = false)),
                inputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, CommandUsage.commandUsage(CreateDate::class))
    }

    @Test
    fun testCommandUsageForApplyKeys()
    {
        val expected = CommandUsage("ApplyKeys",
                arguments = listOf(CommandArgument("keys", DataType.of(DataType.SET, DataType.STRING)),
                        CommandArgument("operation", DataType.of(DataType.OPERATION))),
                inputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, CommandUsage.commandUsage(ApplyKeys::class))
    }

    @Test
    fun testCommandUsageForAdd()
    {
        val expected = CommandUsage("Add",
                arguments = listOf(CommandArgument("entries", DataType.of(DataType.SET, DataType.ENTRY))),
                inputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY),
                outputType = DataType.of(DataType.MAP, DataType.STRING, DataType.ANY))
        assertEquals(expected, CommandUsage.commandUsage(Add::class))
    }
}