// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.R
import com.ichi2.anki.Reviewer
import com.ichi2.anki.dialogs.customstudy.CustomStudyDialog
import com.ichi2.anki.launchCatchingTask
import com.ichi2.anki.snackbar.showSnackbar

/**
 * Renders the shared SvelteKit "Speedrun: Home" dashboard page.
 *
 * The page is bundled in the rsdroid AAR under `backend/sveltekit` and served by
 * [PageFragment]'s in-app server; it is backed by the same engine RPCs wired in
 * [PostRequestHandler].
 *
 * The page's "► START RUN" button calls `bridgeCommand("startrun")`.
 * [onStartRun] resolves the GRE exam deck, checks whether cards are due, and
 * opens the native reviewer — or shows an informational snackbar as a fallback.
 */
class SpeedrunHomeFragment : PageFragment() {
    override val pagePath = "speedrun-home"

    override val bridgeCommands = mapOf("startrun" to { onStartRun() })

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialToolbar>(R.id.toolbar)?.setTitle(R.string.speedrun_home)
        applySpeedrunDarkShell(view)
    }

    private fun onStartRun() {
        launchCatchingTask {
            // 1. Resolve the exam deck by name.
            val did = withCol { decks.idForName("Speedrun::GRE Math") }
            if (did == null) {
                showSnackbar(getString(R.string.speedrun_import_exam_deck))
                return@launchCatchingTask
            }

            // 2. Check whether there are cards due today.
            val node = withCol { sched.deckDueTree().find(did) }
            if (node?.hasCardsReadyToStudy() != true) {
                showSnackbar(
                    getString(R.string.speedrun_all_caught_up),
                    duration = Snackbar.LENGTH_LONG,
                ) {
                    setAction(R.string.speedrun_custom_study) {
                        launchCatchingTask {
                            withCol { decks.select(did) }
                            CustomStudyDialog
                                .createInstance(deckId = did)
                                .show(childFragmentManager, null)
                        }
                    }
                }
                return@launchCatchingTask
            }

            // 3. Cards are ready — select the deck and open the reviewer.
            withCol { decks.select(did) }
            startActivity(Reviewer.getIntent(requireContext()))
        }
    }
}
