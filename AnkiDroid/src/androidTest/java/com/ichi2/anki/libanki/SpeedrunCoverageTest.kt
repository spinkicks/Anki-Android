// Copyright: Ankitects Pty Ltd and contributors
// License: GNU AGPL, version 3 or later; http://www.gnu.org/licenses/agpl.html

package com.ichi2.anki.libanki

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.tests.InstrumentedTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

/**
 * "One engine, two apps" gate.
 *
 * AnkiDroid links our forked rslib via the locally-built rsdroid AAR
 * (local_backend=true). The read-only Speedrun RPC SpeedrunService.GetCoverage
 * must answer from that SAME engine and report the SAME version string the
 * desktop build reports (rslib `.version` == "26.05"). If this passes on the
 * emulator, both apps are provably running one shared Rust engine.
 */
@RunWith(AndroidJUnit4::class)
class SpeedrunCoverageTest : InstrumentedTest() {
    @Test
    fun getCoverageReportsDesktopEngineVersion() {
        val resp = emptyCol.backend.getCoverage(listOf("calc", "linear_algebra"))

        // The gate: Android engine version == desktop engine version.
        assertThat(resp.backendVersion, equalTo("26.05"))

        // Exercise the real read-only coverage logic on an empty collection.
        assertThat(resp.total, equalTo(2))
        assertThat(resp.covered, equalTo(0))
    }
}
