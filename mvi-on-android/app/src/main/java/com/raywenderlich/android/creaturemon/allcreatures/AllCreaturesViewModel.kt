package com.raywenderlich.android.creaturemon.allcreatures

import androidx.lifecycle.ViewModel
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesAction.ClearAllCreaturesAction
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesAction.LoadAllCreaturesAction
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesIntent.ClearAllCreaturesIntent
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesIntent.LoadAllCreaturesIntent
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesResult.ClearAllCreaturesResult
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesResult.LoadAllCreaturesResult
import com.raywenderlich.android.creaturemon.mvibase.MviViewModel
import com.raywenderlich.android.creaturemon.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class AllCreaturesViewModel(
    private val actionProcessorHolder: AllCreaturesProcessorHolder
) : ViewModel(), MviViewModel<AllCreaturesIntent, AllCreaturesViewState> {

    private val intentsSubject: PublishSubject<AllCreaturesIntent> = PublishSubject.create()
    private val statesObservable: Observable<AllCreaturesViewState> = compose()

    private val intentFilter: ObservableTransformer<AllCreaturesIntent, AllCreaturesIntent>
        get() = ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.merge(
                    shared.ofType(LoadAllCreaturesIntent::class.java).take(1),
                    shared.notOfType(LoadAllCreaturesIntent::class.java)
                )
            }
        }

    override fun processIntents(intents: Observable<AllCreaturesIntent>) {
        intents.subscribe(intentsSubject)
    }

    override fun states(): Observable<AllCreaturesViewState> = statesObservable

    private fun compose(): Observable<AllCreaturesViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            .scan(AllCreaturesViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: AllCreaturesIntent): AllCreaturesAction {
        return when (intent) {
            is LoadAllCreaturesIntent -> LoadAllCreaturesAction
            is ClearAllCreaturesIntent -> ClearAllCreaturesAction
        }
    }

    companion object {
        private val reducer = BiFunction { previousState: AllCreaturesViewState, result: AllCreaturesResult ->
            when (result) {
                is LoadAllCreaturesResult -> when (result) {
                    is LoadAllCreaturesResult.Loading -> previousState.copy(isLoading = true)
                    is LoadAllCreaturesResult.Success -> previousState.copy(isLoading = false, creatures = result.creatures)
                    is LoadAllCreaturesResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                }
                is ClearAllCreaturesResult -> when (result) {
                    is ClearAllCreaturesResult.Clearing -> previousState.copy(isLoading = true)
                    is ClearAllCreaturesResult.Success -> previousState.copy(isLoading = false, creatures = emptyList())
                    is ClearAllCreaturesResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}