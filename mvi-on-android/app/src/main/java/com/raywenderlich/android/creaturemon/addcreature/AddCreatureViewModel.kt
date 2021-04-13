package com.raywenderlich.android.creaturemon.addcreature

import androidx.lifecycle.ViewModel
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureAction.*
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureIntent.*
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureResult.*
import com.raywenderlich.android.creaturemon.data.model.CreatureAttributes
import com.raywenderlich.android.creaturemon.data.model.CreatureGenerator
import com.raywenderlich.android.creaturemon.mvibase.MviViewModel
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class AddCreatureViewModel(
    private val actionProcessorHolder: AddCreatureProcessorHolder
) : ViewModel(), MviViewModel<AddCreatureIntent, AddCreatureViewState> {

    private val intentsSubject: PublishSubject<AddCreatureIntent> = PublishSubject.create()
    private val statesObservable: Observable<AddCreatureViewState> = compose()

    override fun processIntents(intents: Observable<AddCreatureIntent>) {
        intents.subscribe(intentsSubject)
    }

    override fun states(): Observable<AddCreatureViewState> = statesObservable

    private fun compose(): Observable<AddCreatureViewState> {
        return intentsSubject
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            .scan(AddCreatureViewState.default(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: AddCreatureIntent): AddCreatureAction {
        return when (intent) {
            is AvatarIntent -> AvatarAction(intent.drawable)
            is NameIntent -> NameAction(intent.name)
            is IntelligenceIntent -> IntelligenceAction(intent.intelligenceIndex)
            is StrengthIntent -> StrengthAction(intent.strengthIndex)
            is EnduranceIntent -> EnduranceAction(intent.enduranceIndex)
            is SaveIntent -> SaveAction(
                drawable = intent.drawable,
                name = intent.name,
                intelligenceIndex = intent.intelligenceIndex,
                strengthIndex = intent.strengthIndex,
                enduranceIndex = intent.enduranceIndex
            )
        }
    }

    companion object {
        private val generator = CreatureGenerator()

        private val reducer = BiFunction { previousState: AddCreatureViewState, result: AddCreatureResult ->
            when (result) {
                is AvatarResult -> reduceAvatar(previousState, result)
                is NameResult -> reduceName(previousState, result)
                is IntelligenceResult -> reduceIntelligence(previousState, result)
                is StrengthResult -> reduceStrength(previousState, result)
                is EnduranceResult -> reduceEndurance(previousState, result)
                is SaveResult -> reduceSave(previousState, result)
            }
        }

        private fun reduceAvatar(
            previousState: AddCreatureViewState,
            result: AvatarResult
        ): AddCreatureViewState = when (result) {
            is AvatarResult.Processing -> previousState.copy(isProcessing = true, error = null)
            is AvatarResult.Success -> previousState.copy(
                isProcessing = false,
                creature = generator.generateCreature(
                    previousState.creature.attributes,
                    previousState.creature.name,
                    result.drawable
                ),
                isSelectedDrawable = (result.drawable != 0),
                error = null
            )
            is AvatarResult.Failure -> previousState.copy(isProcessing = false, error = result.error)
        }

        private fun reduceName(
            previousState: AddCreatureViewState,
            result: NameResult
        ): AddCreatureViewState = when (result) {
            is NameResult.Processing -> previousState.copy(isProcessing = true, error = null)
            is NameResult.Success -> previousState.copy(
                isProcessing = false,
                creature = generator.generateCreature(
                    previousState.creature.attributes,
                    result.name,
                    previousState.creature.drawable
                )
            )
            is NameResult.Failure -> previousState.copy(isProcessing = false, error = result.error)
        }

        private fun reduceIntelligence(
            previousState: AddCreatureViewState,
            result: IntelligenceResult
        ): AddCreatureViewState = when (result) {
            is IntelligenceResult.Processing -> previousState.copy(isProcessing = true, error = null)
            is IntelligenceResult.Success -> {
                val attributes = CreatureAttributes(
                    result.intelligence,
                    previousState.creature.attributes.strength,
                    previousState.creature.attributes.endurance
                )
                previousState.copy(
                    isProcessing = false,
                    creature = generator.generateCreature(
                        attributes,
                        previousState.creature.name,
                        previousState.creature.drawable
                    ),
                    error = null
                )
            }
            is IntelligenceResult.Failure -> previousState.copy(isProcessing = false, error = result.error)
        }

        private fun reduceStrength(
            previousState: AddCreatureViewState,
            result: StrengthResult
        ): AddCreatureViewState = when (result) {
            is StrengthResult.Processing -> previousState.copy(isProcessing = true, error = null)
            is StrengthResult.Success -> {
                val attributes = CreatureAttributes(
                    previousState.creature.attributes.intelligence,
                    result.strength,
                    previousState.creature.attributes.endurance
                )
                previousState.copy(
                    isProcessing = false,
                    creature = generator.generateCreature(
                        attributes,
                        previousState.creature.name,
                        previousState.creature.drawable
                    )
                )
            }
            is StrengthResult.Failure -> previousState.copy(isProcessing = false, error = result.error)
        }

        private fun reduceEndurance(
            previousState: AddCreatureViewState,
            result: EnduranceResult
        ): AddCreatureViewState = when (result) {
            is EnduranceResult.Processing -> previousState.copy(isProcessing = true, error = null)
            is EnduranceResult.Success -> {
                val attributes = CreatureAttributes(
                    previousState.creature.attributes.intelligence,
                    previousState.creature.attributes.strength,
                    result.endurance
                )
                previousState.copy(
                    isProcessing = false,
                    creature = generator.generateCreature(
                        attributes,
                        previousState.creature.name,
                        previousState.creature.drawable
                    )
                )
            }
            is EnduranceResult.Failure -> previousState.copy(isProcessing = false, error = result.error)
        }

        private fun reduceSave(
            previousState: AddCreatureViewState,
            result: SaveResult
        ): AddCreatureViewState = when (result) {
            is SaveResult.Processing -> previousState.copy(isProcessing = true, error = null)
            is SaveResult.Success -> previousState.copy(isProcessing = false, isSaveComplete = true, error = null)
            is SaveResult.Failure -> previousState.copy(isProcessing = false, error = result.error)
        }
    }
}