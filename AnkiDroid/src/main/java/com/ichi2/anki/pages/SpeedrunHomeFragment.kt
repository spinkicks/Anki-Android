// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import com.ichi2.anki.R

/**
 * Renders the shared SvelteKit "Speedrun: Home" dashboard page.
 *
 * The page is bundled in the rsdroid AAR under `backend/sveltekit` and served by
 * [PageFragment]'s in-app server; it is backed by the same engine RPCs wired in
 * [PostRequestHandler].
 *
 * Note: the page's "► START RUN" button calls `pycmd("startrun")`. AnkiDroid has no
 * pycmd bridge in PageFragment, so the call is a safe JS no-op (the page guards with
 * `pycmd?.(…)`). Wiring a full "start studying" action is a follow-up task.
 */
class SpeedrunHomeFragment : PageFragment() {
    override val pagePath = "speedrun-home"

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.toolbar)?.setTitle(R.string.speedrun_home)
        applySpeedrunDarkShell(view)
    }
}
