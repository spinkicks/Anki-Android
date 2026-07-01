/*
 * Copyright (c) 2025 Ankitects Pty Ltd <http://apps.ankiweb.net>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki.libanki

// Speedrun "Memory" dashboard backend RPCs.
// These take and return bytes that the shared SvelteKit frontend encodes/decodes.
// Only the read-only RPCs used by the dashboard are exposed here; the mutating
// `reorderNewByPointsAtStake` RPC is intentionally not wired.
fun Collection.getCoverageRaw(input: ByteArray): ByteArray = backend.getCoverageRaw(input)

fun Collection.getTopicMasteryRaw(input: ByteArray): ByteArray = backend.getTopicMasteryRaw(input)

fun Collection.getExamProfileRaw(input: ByteArray): ByteArray = backend.getExamProfileRaw(input)

fun Collection.getPerformanceReadinessRaw(input: ByteArray): ByteArray = backend.getPerformanceReadinessRaw(input)
