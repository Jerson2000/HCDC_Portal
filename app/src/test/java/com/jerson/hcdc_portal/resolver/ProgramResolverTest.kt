package com.jerson.hcdc_portal.resolver

import com.jerson.hcdc_portal.domain.resolver.ProgramResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProgramResolverTest {

    private lateinit var resolver: ProgramResolver

    @Before
    fun setup() {
        resolver = ProgramResolver()
    }

    @Test
    fun resolveExactCode_returnsProgram() {
        val result = resolver.resolve("BSIT")
        assertNotNull(result)
        assertEquals("BSIT", result?.code)
        assertEquals("Bachelor of Science in Information Technology", result?.canonicalName)
    }

    @Test
    fun resolveWithPunctuation_returnsProgram() {
        val result = resolver.resolve("B.S.I.T.")
        assertNotNull(result)
        assertEquals("BSIT", result?.code)
    }

    @Test
    fun resolveWithHyphen_returnsProgram() {
        val result = resolver.resolve("BS-IT")
        assertNotNull(result)
        assertEquals("BSIT", result?.code)
    }

    @Test
    fun resolveFullName_returnsProgram() {
        val result = resolver.resolve("Bachelor of Science in Information Technology")
        assertNotNull(result)
        assertEquals("BSIT", result?.code)
    }

    @Test
    fun resolveComputerScience_returnsBSCS() {
        val result = resolver.resolve("BSCS")
        assertNotNull(result)
        assertEquals("BSCS", result?.code)
    }

    @Test
    fun resolveComputerEngineering_returnsBSCpE() {
        val result = resolver.resolve("Bachelor of Science in Computer Engineering")
        assertNotNull(result)
        assertEquals("BSCpE", result?.code)
    }

    @Test
    fun resolveCriminology_returnsBSCrim() {
        val result = resolver.resolve("Bachelor of Science in Criminology")
        assertNotNull(result)
        assertEquals("BSCrim", result?.code)
    }

    @Test
    fun resolveInvalid_returnsNull() {
        val result = resolver.resolve("Completely Unknown Major 123")
        assertNull(result)
    }

    @Test
    fun resolveBlank_returnsNull() {
        val result = resolver.resolve("")
        assertNull(result)
    }
}
