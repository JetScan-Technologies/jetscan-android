package io.github.dracula101.jetscan.data.platform.manager.special_circumstance

import io.github.dracula101.jetscan.data.platform.manager.models.SpecialCircumstance
import kotlinx.coroutines.flow.Flow

/**
 * Tracks any [SpecialCircumstance] that may be present.
 *
 * Note that this will be scoped to the current "retained Activity": if there are multiple tasks
 * that each have a [MainActivity], they can each have a separate [SpecialCircumstance] associated
 * with them.
 */
interface SpecialCircumstanceManager {
    /**
     * Gets the current [SpecialCircumstance] if any.
     */
    var specialCircumstance: SpecialCircumstance?

    /**
     * Emits updates that track changes to [specialCircumstance].
     */
    val specialCircumstanceFlow: Flow<SpecialCircumstance?>

}
