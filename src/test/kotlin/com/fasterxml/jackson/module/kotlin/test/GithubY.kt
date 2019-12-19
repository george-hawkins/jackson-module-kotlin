package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
interface Base

object Child : Base

class TestGithubY {
    private val json = """
        [ {
          "@type" : ".Child",
          "@id" : 1
        }, 1 ]
    """.trimIndent()

    @Test
    fun `test writing a list of objects with id`() {
        val input = listOf<Base>(Child, Child)

        val output = jacksonObjectMapper()
            .writerFor(jacksonTypeRef<List<Base>>())
            .withDefaultPrettyPrinter()
            .writeValueAsString(input)

        assertEquals(json, output)
    }

    @Test
    fun `test reading a list of objects with id`() {
        val output = jacksonObjectMapper().readValue<List<Base>>(json)

        assertEquals(2, output.size)
        val (a, b) = output

        // The first assert here passes as the element is deserialized via KotlinObjectSingletonDeserializer.
        // The seconds assert fails as KotlinObjectSingletonDeserializer is bypassed for retrieval by ID.
        assertSame(Child, a, "first element is not the same instance as Child")
        assertSame(Child, b, "second element is not the same instance as Child")
    }
}
