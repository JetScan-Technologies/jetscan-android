package io.github.dracula101.jetscan.data.auth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserState(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
) : Parcelable