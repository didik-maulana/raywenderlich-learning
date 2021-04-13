package com.raywenderlich.android.creaturemon.addcreature

import com.raywenderlich.android.creaturemon.mvibase.MviAction

sealed class AddCreatureAction : MviAction {
    data class AvatarAction(val drawable: Int) : AddCreatureAction()
    data class NameAction(val name: String) : AddCreatureAction()
    data class IntelligenceAction(val intelligenceIndex: Int) : AddCreatureAction()
    data class StrengthAction(val strengthIndex: Int) : AddCreatureAction()
    data class EnduranceAction(val enduranceIndex: Int) : AddCreatureAction()
    data class SaveAction(
        val drawable: Int,
        val name: String,
        val intelligenceIndex: Int,
        val strengthIndex: Int,
        val enduranceIndex: Int
    ) : AddCreatureAction()
}