package com.example.test.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.Materia
import com.example.test.data.repository.MateriaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MateriaViewModel(private val repository: MateriaRepository) : ViewModel() {

    val materias: LiveData<List<Materia>> = repository.allMaterias

    // LiveData para las materias recientes
    val recentMaterias: LiveData<List<Materia>> = repository.getRecentOrFrequentlyUsedMaterias()

    private val _insercionExitosa = MutableLiveData<Boolean>()
    val insercionExitosa: LiveData<Boolean> = _insercionExitosa

    // Insertar nueva
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

    // Buscar
    fun buscarMaterias(query: String): LiveData<List<Materia>> {
        return repository.searchMaterias(query)
    }

    // Actualizar fecha de uso (Clic normal)
    fun updateMateriaInteraction(materia: Materia) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMateria = materia.copy(ultimaInteraccion = System.currentTimeMillis())
            repository.updateMateria(updatedMateria)
        }
    }

    // Eliminar
    fun eliminarMateria(materia: Materia) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminarMateria(materia)
        }
    }

    // --- ¡NUEVA FUNCIÓN! Actualizar Nombre ---
    fun actualizarMateria(materia: Materia) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMateria(materia)
        }
    }

    // Mantenemos esta por compatibilidad si alguna vista vieja la usa
    fun agregarMateria(materia: Materia) = insertMateria(materia)
}