package com.example.test.data.repository

import androidx.lifecycle.LiveData
import com.example.test.data.dao.TareaDao
import com.example.test.data.model.Tarea
import com.example.test.data.model.Materia


class TareaRepository(private val dao: TareaDao) {



    fun buscarMateria(id: Int): LiveData<Materia>{
        return dao.buscarMateriaPorId(id)
    }

    fun buscarTareas(nombre:String): LiveData<List<Tarea>>{
        return dao.buscarTareasPorNombre(nombre)
    }

    fun buscarTareasPorEstado(estado:Boolean):LiveData<List<Tarea>>{
        return dao.buscarTareasPorEstado(estado)
    }

    // En TareaRepository.kt
    fun getTareasByMateriaId(materiaId: Int): LiveData<List<Tarea>> {
        return dao.getTareasByMateriaId(materiaId)
    }

    suspend fun cambiarEstadoTarea(id: Int, estado: Boolean) = dao.cambiarEstadoTarea(id,estado)


    val tareas: LiveData<List<Tarea>> = dao.obtenerTareas()

    suspend fun agregarTarea(tarea:Tarea) = dao.agregarTarea(tarea)
    suspend fun actualizarTarea(tarea:Tarea) = dao.actualizarTarea(tarea)
    suspend fun eliminarTarea(tarea: Tarea) = dao.eliminarTarea(tarea)

}