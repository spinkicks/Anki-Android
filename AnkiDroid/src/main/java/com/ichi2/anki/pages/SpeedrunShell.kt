// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.MaterialToolbar
import com.ichi2.anki.R

/**
 * Applies the Speedrun dark-shell theming to the two Speedrun screens:
 * - Toolbar background set to [R.color.speedrun_shell_bg] (#0B0E12)
 * - Toolbar title + navigation icon tinted white
 * - Activity window status-bar and navigation-bar set to the same dark colour
 *   with light (white) icons
 *
 * Scoped entirely to the calling fragment's [onViewCreated]; no global theme is changed.
 * The [SingleFragmentActivity] hosts exactly one fragment at a time so these window-level
 * changes only affect Speedrun screens while they are visible.
 */
internal fun PageFragment.applySpeedrunDarkShell(view: View) {
    val shellColor = ContextCompat.getColor(requireContext(), R.color.speedrun_shell_bg)
    val white = Color.WHITE

    // Toolbar background + tint
    view.findViewById<MaterialToolbar>(R.id.toolbar)?.apply {
        setBackgroundColor(shellColor)
        setTitleTextColor(white)
        navigationIcon?.setTint(white)
    }

    // Status bar: dark background, light (white) icons
    val window = requireActivity().window
    @Suppress("deprecation", "API35 properly handle edge-to-edge")
    window.statusBarColor = shellColor
    WindowInsetsControllerCompat(window, window.decorView).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
    }

    // Navigation bar: dark background, light icons
    @Suppress("deprecation", "API35 properly handle edge-to-edge")
    window.navigationBarColor = shellColor
}
