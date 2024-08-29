package io.github.dracula101.jetscan.data.auth.util

import com.google.firebase.auth.FirebaseUser
import io.github.dracula101.jetscan.data.auth.model.UserState

fun FirebaseUser.toUserState() : UserState {
    return UserState(
        uid = this.uid,
        email = this.email ?: "",
        displayName = this.displayName ?: "",
        photoUrl = this.photoUrl.toString(),
    )
}