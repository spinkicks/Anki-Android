// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.content.Context
import android.content.Intent
import com.ichi2.anki.SingleFragmentActivity
import com.ichi2.anki.common.destinations.SpeedrunMemoryDestination

/** Builds the [Intent] that opens the Speedrun "Memory" dashboard screen. */
fun SpeedrunMemoryDestination.toIntent(context: Context): Intent =
    SingleFragmentActivity.getIntent(context, fragmentClass = SpeedrunMemoryFragment::class)
