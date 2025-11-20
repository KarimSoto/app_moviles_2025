package com.example.test.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.Materia
import com.example.test.data.repository.MateriaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MateriaViewModel(private val repository: MateriaRepository) : ViewModel() {

    val materias: LiveData<List<Materia>> = repository.allMaterias

    // ¡NUEVA LIVE DATA! Para las 4 materias recientes
    val recentMaterias: LiveData<List<Materia>> = repository.getRecentOrFrequentlyUsedMaterias()

    private val _insercionExitosa = androidx.lifecycle.MutableLiveData<Boolean>()
    val insercionExitosa: LiveData<Boolean> = _insercionExitosa

    fun insertMateria(materia: Materia) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertMateria(materia)
                _insercionExitosa.postValue(true)
            } catch (e: Exception) {
                _insercionExitosa.postValue(false)
            }
        }
    }

    fun agregarMateria(materia: Materia) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMateria(materia)
        }
    }

    fun buscarMaterias(query: String): LiveData<List<Materia>> {
        return repository.searchMaterias(query)
    }

    // ¡NUEVA FUNCIÓN! Para registrar que una materia fue usada
    fun updateMateriaInteraction(materia: Materia) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMateria = materia.copy(ultimaInteraccion = System.currentTimeMillis())
            repository.updateMateria(updatedMateria)
        }
    }
}