package io.github.dracula101.jetscan.data.platform.manager.special_circumstance

import io.github.dracula101.jetscan.data.auth.model.AuthState
import io.github.dracula101.jetscan.data.auth.repository.AuthRepository
import io.github.dracula101.jetscan.data.platform.manager.models.SpecialCircumstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Primary implementation of [SpecialCircumstanceManager].
 */
class SpecialCircumstanceManagerImpl(
    private val authRepository: AuthRepository,
) : SpecialCircumstanceManager {

    private val mutableSpecialCircumstanceFlow = MutableStateFlow<SpecialCircumstance?>(null)
    private val unconfinedScope = CoroutineScope(Dispatchers.Unconfined)

    init {
        authRepository
            .authStateFlow
            .onEach { user ->
            }
            .launchIn(unconfinedScope)
    }

    override var specialCircumstance: SpecialCircumstance?
        get() = mutableSpecialCircumstanceFlow.value
        set(value) {
            mutableSpecialCircumstanceFlow.value = value
        }

    override val specialCircumstanceStateFlow: StateFlow<SpecialCircumstance?>
        get() = mutableSpecialCircumstanceFlow

}
