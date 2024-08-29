package io.github.dracula101.jetscan.data.platform.utils

import android.util.Patterns

object Validators {

    fun String.isValidEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

}