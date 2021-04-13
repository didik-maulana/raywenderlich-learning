package com.raywenderlich.android.creaturemon.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureProcessorHolder
import com.raywenderlich.android.creaturemon.addcreature.AddCreatureViewModel
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesProcessorHolder
import com.raywenderlich.android.creaturemon.allcreatures.AllCreaturesViewModel
import com.raywenderlich.android.creaturemon.app.Injection

class CreaturemonViewModelFactory private constructor(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AllCreaturesViewModel::class.java -> {
                AllCreaturesViewModel(
                    AllCreaturesProcessorHolder(
                        Injection.provideCreatureRepository(applicationContext),
                        Injection.provideSchedulerProvider()
                    )
                ) as T
            }
            AddCreatureViewModel::class.java -> {
                AddCreatureViewModel(
                    AddCreatureProcessorHolder(
                        Injection.provideCreatureRepository(applicationContext),
                        Injection.provideCreatureGenerator(),
                        Injection.provideSchedulerProvider()
                    )
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown model class $modelClass")
        }
    }

    companion object : SingletonHolderSingleArg<CreaturemonViewModelFactory, Context>(::CreaturemonViewModelFactory)
}