package io.github.dracula101.jetscan.data.platform.manager.exit

import androidx.compose.runtime.Immutable

/**
 * A manager class for handling the various ways to exit the app.
 */
@Immutable
interface ExitManager {
    /**
     * Finishes the activity.
     */
    fun exitApplication()
}
