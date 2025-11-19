package com.example.test.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.Materia
import com.example.test.data.model.Tarea
import com.example.test.data.repository.MateriaRepository
import kotlinx.coroutines.launch


class MateriaViewModel(private val repository: MateriaRepository) : ViewModel(){

    val materias = repository.materias


    private val _insercionExitosa = MutableLiveData<Boolean>()
    val insercionExitosa: LiveData<Boolean> get() = _insercionExitosa


    fun buscarMaterias(nombre:String):LiveData<List<Materia>>{
        return repository.buscarMaterias(nombre)
    }

    fun agregarMateria(materia: Materia) = viewModelScope.launch{
        try{
            repository.agregarMateria(materia)
            _insercionExitosa.postValue(true)
        }catch(e:Exception){
            _insercionExitosa.postValue(false)
        }
    }

    fun actualizarMateria(materia: Materia) = viewModelScope.launch {
        repository.actualizarMateria(materia)
    }

    fun eliminarMateria(materia: Materia) = viewModelScope.launch{
        repository.eliminarMateria(materia)
    }
}