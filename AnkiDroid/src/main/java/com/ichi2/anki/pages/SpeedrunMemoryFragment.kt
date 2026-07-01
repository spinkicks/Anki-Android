// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import com.ichi2.anki.R

/**
 * Renders the shared SvelteKit "Speedrun: Memory" dashboard page.
 *
 * The page is bundled in the rsdroid AAR under `backend/sveltekit` and served by
 * [PageFragment]'s in-app server; it is backed by the same engine RPCs wired in
 * [PostRequestHandler].
 */
class SpeedrunMemoryFragment : PageFragment() {
    override val pagePath = "speedrun-memory"

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.toolbar)?.setTitle(R.string.speedrun_memory)
    }
}
