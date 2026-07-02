// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.os.Bundle
import android.view.View
import anki.decks.Deck
import anki.decks.DeckKt.FilteredKt.searchTerm
import anki.decks.DeckKt.filtered
import anki.decks.filteredDeckForUpdate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.ichi2.anki.CollectionManager.withCol
import com.ichi2.anki.R
import com.ichi2.anki.Reviewer
import com.ichi2.anki.dialogs.customstudy.CustomStudyDialog
import com.ichi2.anki.launchCatchingTask
import com.ichi2.anki.libanki.DeckId
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
 *
 * The "MINI-MOCK" button calls `bridgeCommand("minimock")`; [onMiniMock] builds a
 * filtered deck of random problems over the `Speedrun::GRE Math::Problems` subdeck
 * (with `reschedule=true` so attempts count) and opens the reviewer, mirroring
 * desktop's `build_mini_mock_deck` (`qt/aqt/speedrun_logic.py`).
 *
 * The Home status banner fires `startrun:customstudy` (open Custom Study on the
 * exam deck) and `startrun:import` (import the exam deck); see [onCustomStudy] and
 * [onImport].
 */
class SpeedrunHomeFragment : PageFragment() {
    override val pagePath = "speedrun-home"

    override val bridgeCommands =
        mapOf(
            "startrun" to { onStartRun() },
            "minimock" to { onMiniMock() },
            "startrun:customstudy" to { onCustomStudy() },
            "startrun:import" to { onImport() },
        )

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
            val did = withCol { decks.idForName(EXAM_DECK) }
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
                        showCustomStudy(did)
                    }
                }
                return@launchCatchingTask
            }

            // 3. Cards are ready — select the deck and open the reviewer.
            withCol { decks.select(did) }
            startActivity(Reviewer.getIntent(requireContext()))
        }
    }

    /**
     * Handles the "MINI-MOCK" button (`bridgeCommand("minimock")`).
     *
     * Mirrors desktop's `_start_mini_mock`/`build_mini_mock_deck`: builds (or refreshes)
     * the "Speedrun Mini-Mock" filtered deck pulling up to a config-driven number of
     * RANDOM, non-suspended cards from the `Speedrun::GRE Math::Problems` subdeck, then
     * opens the native reviewer on it.
     *
     * `reschedule` MUST be true so mock attempts feed scoring: the engine only counts
     * revlog entries where `has_rating() && !is_cramming()`, and a `reschedule=false`
     * filtered deck is preview/cram mode (excluded from scores). See the docstring on
     * `build_mini_mock_deck` in `qt/aqt/speedrun_logic.py`.
     *
     * If the Problems subdeck is missing, shows the same honest import snackbar as
     * [onStartRun].
     */
    private fun onMiniMock() {
        launchCatchingTask {
            // 1. Resolve the Problems subdeck by name.
            val problemsDid = withCol { decks.idForName(PROBLEM_DECK) }
            if (problemsDid == null) {
                showSnackbar(getString(R.string.speedrun_import_exam_deck))
                return@launchCatchingTask
            }

            // 2. Config-driven mock size (Decision 13 default 10; synced col config),
            //    never hard-coded. Matches desktop's "speedrun:mini_mock_size" key.
            val size =
                withCol {
                    config.get<Int>(MINI_MOCK_SIZE_KEY, MINI_MOCK_DEFAULT_SIZE)
                } ?: MINI_MOCK_DEFAULT_SIZE

            // 3. Build/refresh the filtered deck via the backend (undo-safe op).
            val did =
                withCol {
                    val update =
                        filteredDeckForUpdate {
                            // backend uses 0 to mean "create a new filtered deck"
                            id = 0
                            name = MINI_MOCK_DECK_NAME
                            config =
                                filtered {
                                    // see docstring: mock attempts must score
                                    reschedule = true
                                    searchTerms.add(
                                        searchTerm {
                                            search = """deck:"$PROBLEM_DECK" -is:suspended"""
                                            limit = size
                                            order = Deck.Filtered.SearchTerm.Order.RANDOM
                                        },
                                    )
                                }
                        }
                    val out = sched.addOrUpdateFilteredDeck(update)
                    // OpChangesWithId.id carries the new/updated deck id; fall back to
                    // resolving by name defensively if the op didn't carry one.
                    out.id.takeIf { it != 0L } ?: decks.idForName(MINI_MOCK_DECK_NAME)
                }

            if (did == null) {
                showSnackbar(getString(R.string.speedrun_import_exam_deck))
                return@launchCatchingTask
            }

            // 4. Select the mock deck and open the reviewer.
            withCol { decks.select(did) }
            startActivity(Reviewer.getIntent(requireContext()))
        }
    }

    /**
     * Handles the status banner's `startrun:customstudy` command: selects the exam deck
     * and opens the real Custom Study dialog (mirrors [onStartRun]'s snackbar action).
     * Shows the honest import snackbar if the exam deck is missing.
     */
    private fun onCustomStudy() {
        launchCatchingTask {
            val did = withCol { decks.idForName(EXAM_DECK) }
            if (did == null) {
                showSnackbar(getString(R.string.speedrun_import_exam_deck))
                return@launchCatchingTask
            }
            showCustomStudy(did)
        }
    }

    /**
     * Handles the status banner's `startrun:import` command.
     *
     * The in-app apkg file picker ([com.ichi2.anki.dialogs.ImportFileSelectionFragment.openImportFilePicker])
     * requires the host activity to implement `ApkgImportResultLauncherProvider`, which
     * only [com.ichi2.anki.DeckPicker] does — this page is hosted by a plain
     * [com.ichi2.anki.SingleFragmentActivity], so that launcher is not cleanly reachable
     * from here. Rather than invent an API, we show the honest import snackbar directing
     * the user to import the exam deck (import is available from the deck picker).
     */
    private fun onImport() {
        showSnackbar(getString(R.string.speedrun_import_exam_deck))
    }

    /** Selects [did] and shows the native Custom Study dialog for it. */
    private fun showCustomStudy(did: DeckId) {
        launchCatchingTask {
            withCol { decks.select(did) }
            CustomStudyDialog
                .createInstance(deckId = did)
                .show(childFragmentManager, null)
        }
    }

    companion object {
        /** The exam deck the run studies. */
        private const val EXAM_DECK = "Speedrun::GRE Math"

        /** The Problems subdeck a timed mini-mock draws from (seeded bank). */
        private const val PROBLEM_DECK = "Speedrun::GRE Math::Problems"

        /** Name of the reusable filtered deck built for the mini-mock. */
        private const val MINI_MOCK_DECK_NAME = "Speedrun Mini-Mock"

        /** Synced collection config key for the mock size (matches desktop). */
        private const val MINI_MOCK_SIZE_KEY = "speedrun:mini_mock_size"

        /** Default mock size when the config key is unset (Decision 13). */
        private const val MINI_MOCK_DEFAULT_SIZE = 10
    }
}
