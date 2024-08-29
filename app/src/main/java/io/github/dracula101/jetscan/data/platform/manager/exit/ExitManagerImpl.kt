package io.github.dracula101.jetscan.data.platform.manager.exit

import android.app.Activity

/**
 * The default implementation of the [ExitManager] for managing the various ways to exit the app.
 */
class ExitManagerImpl(
    val activity: Activity,
) : ExitManager {
    override fun exitApplication() {
        activity.finish()
    }
}
