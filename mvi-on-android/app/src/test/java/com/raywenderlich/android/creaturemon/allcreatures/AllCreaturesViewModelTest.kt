package com.raywenderlich.android.creaturemon.allcreatures

import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesIntent.ClearAllCreaturesIntent
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesIntent.LoadAllCreaturesIntent
import com.raywenderlich.android.creaturemon.data.model.Creature
import com.raywenderlich.android.creaturemon.data.model.CreatureAttributes
import com.raywenderlich.android.creaturemon.data.model.CreatureGenerator
import com.raywenderlich.android.creaturemon.data.repository.CreatureRepository
import com.raywenderlich.android.creaturemon.util.schedulers.BaseSchedulerProvider
import com.raywenderlich.android.creaturemon.util.schedulers.ImmediateSchedulerProvider
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class AllCreaturesViewModelTest {

    @Mock
    private lateinit var creatureRepository: CreatureRepository
    private lateinit var schedulerProvider: BaseSchedulerProvider
    private lateinit var generator: CreatureGenerator
    private lateinit var viewModel: AllCreaturesViewModel
    private lateinit var testObserver: TestObserver<AllCreaturesViewState>
    private lateinit var creatures: List<Creature>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        schedulerProvider = ImmediateSchedulerProvider()
        generator = CreatureGenerator()
        viewModel = AllCreaturesViewModel(AllCreaturesProcessorHolder(
            creatureRepository,
            schedulerProvider
        ))
        creatures = listOf(
            generator.generateCreature(CreatureAttributes(3, 7, 10), "Creature 1", 1),
            generator.generateCreature(CreatureAttributes(7, 10, 3), "Creature 2", 1),
            generator.generateCreature(CreatureAttributes(10, 3, 7), "Creature 3", 1)
        )
        testObserver = viewModel.states().test()
    }

    @Test
    fun loadAllCreaturesFromRepositoryAndLoadIntoView() {
        `when`(creatureRepository.getAllCreatures()).thenReturn(Observable.just(creatures))

        viewModel.processIntents(Observable.just(LoadAllCreaturesIntent))

        testObserver.assertValueAt(1, AllCreaturesViewState::isLoading)
        testObserver.assertValueAt(2) { viewState ->
            !viewState.isLoading
        }
    }

    @Test
    fun loadAllCreaturesFromRepositorySuccess() {
        `when`(creatureRepository.getAllCreatures()).thenReturn(Observable.just(creatures))

        viewModel.processIntents(Observable.just(LoadAllCreaturesIntent))

        testObserver.assertValueAt(2) { viewState ->
            viewState.creatures == creatures
        }
    }

    @Test
    fun errorLoadingCreaturesShowError() {
        `when`(creatureRepository.getAllCreatures()).thenReturn(Observable.error(Exception()))

        viewModel.processIntents(Observable.just(LoadAllCreaturesIntent))

        testObserver.assertValueAt(2) { viewState ->
            viewState.error != null
        }
    }

    @Test
    fun clearAllCreaturesFromRepositoryAndShowLoading() {
        `when`(creatureRepository.clearAllCreatures()).thenReturn(Observable.just(true))

        viewModel.processIntents(Observable.just(ClearAllCreaturesIntent))

        testObserver.assertValueAt(1, AllCreaturesViewState::isLoading)
    }

    @Test
    fun clearAllCreaturesSuccess() {
        `when`(creatureRepository.clearAllCreatures()).thenReturn(Observable.just(true))

        viewModel.processIntents(Observable.just(ClearAllCreaturesIntent))

        testObserver.assertValueAt(2) { viewState ->
            viewState.creatures.isEmpty()
        }
    }

    @Test
    fun clearAllCreaturesErrorAndReturnThrowable() {
        `when`(creatureRepository.clearAllCreatures()).thenReturn(Observable.error(Exception()))

        viewModel.processIntents(Observable.just(ClearAllCreaturesIntent))

        testObserver.assertValueAt(2) { viewState ->
            viewState.error != null
        }
    }
}