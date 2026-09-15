package com.beatoraja.screenshot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionComparatorTest {

    @Test
    void normalizeStripsLeadingV() {
        assertEquals("1.3.0", VersionComparator.normalize("v1.3.0"));
    }

    @Test
    void comparePatchVersions() {
        assertTrue(VersionComparator.isNewer("1.3.1", "1.3.0"));
        assertFalse(VersionComparator.isNewer("1.3.0", "1.3.0"));
        assertFalse(VersionComparator.isNewer("1.2.9", "1.3.0"));
    }

    @Test
    void compareMinorVersions() {
        assertTrue(VersionComparator.isNewer("1.4.0", "1.3.0"));
        assertTrue(VersionComparator.isNewer("2.0.0", "1.9.9"));
    }
}
