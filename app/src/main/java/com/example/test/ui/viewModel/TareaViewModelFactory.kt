package com.example.test.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test.data.repository.TareaRepository

class TareaViewModelFactory(private val repository: TareaRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(TareaViewModel::class.java)){
            return TareaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}