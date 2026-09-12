package com.jerson.hcdc_portal.resolver

import com.jerson.hcdc_portal.domain.model.CanonicalProgram
import com.jerson.hcdc_portal.domain.model.ProspectusVersion
import com.jerson.hcdc_portal.domain.resolver.ProgramResolver
import com.jerson.hcdc_portal.domain.resolver.ProspectusResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProspectusResolverTest {

    private lateinit var programResolver: ProgramResolver
    private lateinit var prospectusResolver: ProspectusResolver

    @Before
    fun setup() {
        programResolver = ProgramResolver()
        prospectusResolver = ProspectusResolver(programResolver)
    }

    @Test
    fun resolveBSIT_returnsCorrectEndpointAndVersion() {
        val resolution = prospectusResolver.resolveProspectusUrl("Bachelor of Science in Information Technology")
        assertNotNull(resolution)
        assertEquals("BSIT", resolution?.program?.code)
        assertEquals("03000000-B01B-0157-24F6-DD5C30F25AF2", resolution?.selectedVersion?.prospectusUuid)
        assertTrue(resolution?.endpointUrl?.contains("03000000-B01B-0157-24F6-DD5C30F25AF2") == true)
        assertTrue(resolution?.endpointUrl?.endsWith("/1") == true)
    }

    @Test
    fun resolveBSCS_returnsBSCSProspectusUrl() {
        val resolution = prospectusResolver.resolveProspectusUrl("BSCS")
        assertNotNull(resolution)
        assertEquals("BSCS", resolution?.program?.code)
        assertEquals("03000000-B42F-0183-9C8C-A0BAAE798383", resolution?.selectedVersion?.prospectusUuid)
    }

    @Test
    fun resolveAcademicYear_selectsMatchingVersionWhenAvailable() {
        val testProgram = CanonicalProgram(
            code = "TEST_PROG",
            canonicalName = "Test Program",
            aliases = listOf("TEST"),
            versions = listOf(
                ProspectusVersion(academicYear = "2022-2023", prospectusUuid = "uuid-2022", isDefault = false),
                ProspectusVersion(academicYear = "2023-2024", prospectusUuid = "uuid-2023", isDefault = true),
                ProspectusVersion(academicYear = "2024-2025", prospectusUuid = "uuid-2024", isDefault = false)
            )
        )
        programResolver.updateCatalog(listOf(testProgram))

        val resolved2022 = prospectusResolver.resolveProspectusUrl("TEST", preferredAy = "2022-2023")
        assertEquals("uuid-2022", resolved2022?.selectedVersion?.prospectusUuid)

        val resolvedDefault = prospectusResolver.resolveProspectusUrl("TEST")
        assertEquals("uuid-2023", resolvedDefault?.selectedVersion?.prospectusUuid)

        val resolved2024 = prospectusResolver.resolveProspectusUrl("TEST", preferredAy = "2024-2025")
        assertEquals("uuid-2024", resolved2024?.selectedVersion?.prospectusUuid)
    }

    @Test
    fun resolveUnknownProgram_returnsNull() {
        val resolution = prospectusResolver.resolveProspectusUrl("NonExistentProgramXYZ")
        assertNull(resolution)
    }
}
