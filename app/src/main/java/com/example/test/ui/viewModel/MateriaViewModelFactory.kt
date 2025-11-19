package com.example.test.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test.data.repository.MateriaRepository

class MateriaViewModelFactory(private val repository: MateriaRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(MateriaViewModel::class.java)){
            return MateriaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}