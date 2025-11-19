package com.example.test.data.repository

import androidx.lifecycle.LiveData
import com.example.test.data.dao.MateriaDao
import com.example.test.data.model.Materia

class MateriaRepository(val dao: MateriaDao) {

    val materias: LiveData<List<Materia>> = dao.obtenerMaterias()

    fun buscarMaterias(nombre:String): LiveData<List<Materia>>{
        return dao.buscarMateriasPorNombre(nombre)
    }

    suspend fun agregarMateria(materia:Materia) = dao.agregarMateria(materia)
    suspend fun actualizarMateria(materia:Materia) = dao.actualizarMateria(materia)
    suspend fun eliminarMateria(materia:Materia) = dao.eliminarMateria(materia)

}