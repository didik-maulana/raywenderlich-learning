package com.raywenderlich.android.creaturemon.addcreature

import com.raywenderlich.android.creaturemon.data.model.Creature
import com.raywenderlich.android.creaturemon.data.model.CreatureAttributes
import com.raywenderlich.android.creaturemon.data.model.CreatureGenerator
import com.raywenderlich.android.creaturemon.mvibase.MviViewState

data class AddCreatureViewState(
    val isProcessing: Boolean,
    val creature: Creature,
    val isSelectedDrawable: Boolean,
    val isSaveComplete: Boolean,
    val error: Throwable?
) : MviViewState {

    companion object {
        fun default(): AddCreatureViewState = AddCreatureViewState(
            isProcessing = false,
            creature = CreatureGenerator().generateCreature(CreatureAttributes(), "", 0),
            isSelectedDrawable = false,
            isSaveComplete = false,
            error = null
        )
    }
}