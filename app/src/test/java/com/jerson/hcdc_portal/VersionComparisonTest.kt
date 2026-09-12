package com.jerson.hcdc_portal

import com.jerson.hcdc_portal.data.repository.AppRepositoryImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparisonTest {

    @Test
    fun isVersionNewer_majorVersionHigher_returnsTrue() {
        assertTrue(AppRepositoryImpl.isVersionNewer("2.0.0", "1.9.9"))
    }

    @Test
    fun isVersionNewer_minorVersionHigher_returnsTrue() {
        assertTrue(AppRepositoryImpl.isVersionNewer("1.10.0", "1.9.0"))
    }

    @Test
    fun isVersionNewer_patchVersionHigher_returnsTrue() {
        assertTrue(AppRepositoryImpl.isVersionNewer("1.0.1", "1.0.0"))
    }

    @Test
    fun isVersionNewer_sameVersion_returnsFalse() {
        assertFalse(AppRepositoryImpl.isVersionNewer("1.0.0", "1.0.0"))
    }

    @Test
    fun isVersionNewer_olderVersion_returnsFalse() {
        assertFalse(AppRepositoryImpl.isVersionNewer("1.0.0", "1.0.1"))
        assertFalse(AppRepositoryImpl.isVersionNewer("1.9.0", "1.10.0"))
        assertFalse(AppRepositoryImpl.isVersionNewer("0.9.9", "1.0.0"))
    }

    @Test
    fun isVersionNewer_withVPrefixAndSuffix_parsedCorrectly() {
        assertTrue(AppRepositoryImpl.isVersionNewer("v2.1.0-release", "v2.0.5"))
    }

    @Test
    fun isVersionNewer_differentSegmentLengths() {
        assertTrue(AppRepositoryImpl.isVersionNewer("1.2.1", "1.2"))
        assertFalse(AppRepositoryImpl.isVersionNewer("1.2", "1.2.1"))
    }
}
