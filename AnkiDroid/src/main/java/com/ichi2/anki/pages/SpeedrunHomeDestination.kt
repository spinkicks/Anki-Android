// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.pages

import android.content.Context
import android.content.Intent
import com.ichi2.anki.SingleFragmentActivity
import com.ichi2.anki.common.destinations.SpeedrunHomeDestination

/** Builds the [Intent] that opens the Speedrun "Home" dashboard screen. */
fun SpeedrunHomeDestination.toIntent(context: Context): Intent =
    SingleFragmentActivity.getIntent(context, fragmentClass = SpeedrunHomeFragment::class)
