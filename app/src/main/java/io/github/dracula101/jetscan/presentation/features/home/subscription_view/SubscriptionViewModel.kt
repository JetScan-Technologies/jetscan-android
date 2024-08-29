package io.github.dracula101.jetscan.presentation.features.home.subscription_view


import android.content.ContentResolver
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.dracula101.jetscan.presentation.platform.base.BaseViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

const val SUBSCRIPTION_STATE = ""

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver,
) : BaseViewModel<SubscriptionState, Unit, SubscriptionAction>(
    initialState = savedStateHandle[SUBSCRIPTION_STATE] ?: SubscriptionState(),
) {
    override fun handleAction(action: SubscriptionAction) {
    }
}


@Parcelize
data class SubscriptionState(
    val isLoading: Boolean = true
) : Parcelable {

    sealed class SubscriptionDialogState : Parcelable {}

}

sealed class SubscriptionAction {

    @Parcelize
    sealed class Ui : SubscriptionAction(), Parcelable {}

    @Parcelize
    sealed class Alerts : SubscriptionAction(), Parcelable {}
}