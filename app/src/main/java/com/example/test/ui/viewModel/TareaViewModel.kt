package com.example.test.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.model.Materia
import com.example.test.data.model.Tarea
import kotlinx.coroutines.launch
import com.example.test.data.repository.TareaRepository

class TareaViewModel(val repository: TareaRepository): ViewModel() {

    val tareas = repository.tareas

    private val _insercionExitosa = MutableLiveData<Boolean>()
    val insercionExitosa: LiveData<Boolean> get() = _insercionExitosa

    private val _queryExitoso = MutableLiveData<Boolean>()
    val queryExitoso: LiveData<Boolean> get() = _queryExitoso

    fun buscarMateria(id: Int): LiveData<Materia>{
        return repository.buscarMateria(id)
    }

    fun buscarTareas(nombre: String): LiveData<List<Tarea>>{
        return repository.buscarTareas(nombre)
    }

    fun buscarTareasPorEstado(estado:Boolean): LiveData<List<Tarea>>{
        return repository.buscarTareasPorEstado(estado)
    }

    fun agregarTarea(tarea: Tarea) = viewModelScope.launch{
        try{
            repository.agregarTarea(tarea)
            _insercionExitosa.postValue(true)
        }catch(e: Exception){
            _insercionExitosa.postValue(false)
        }
    }

    fun cambiarEstadoTarea(id: Int, estado: Boolean) = viewModelScope.launch{
        try{
            repository.cambiarEstadoTarea(id,estado)
            _queryExitoso.postValue(true)
        }
        catch(e: Exception){
            _queryExitoso.postValue(false)
        }
    }

    fun actualizarTarea(tarea: Tarea) = viewModelScope.launch{
        repository.actualizarTarea(tarea)
    }

    fun eliminarTarea(tarea: Tarea) = viewModelScope.launch{
        repository.eliminarTarea(tarea)
    }
}