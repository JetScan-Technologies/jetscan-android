package io.github.dracula101.jetscan.data.auth.util


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class GoogleSignInContract(
    private val googleSignInClientIntent: Intent,
    private val onGoogleSignInWithIntent: (Intent) -> Unit,
    private val onGoogleSignInCancelled: () -> Unit,
) :
    ActivityResultContract<Int, Int>() {
    override fun parseResult(resultCode: Int, intent: Intent?): Int {
        if (resultCode == Activity.RESULT_CANCELED) {
            onGoogleSignInCancelled()
        } else if (resultCode == Activity.RESULT_OK) {
            onGoogleSignInWithIntent(intent!!)
        }
        return resultCode
    }

    override fun createIntent(context: Context, input: Int): Intent {
        return googleSignInClientIntent
    }
}