package com.example.test.data.repository

import androidx.lifecycle.LiveData
import com.example.test.data.dao.MateriaDao
import com.example.test.data.model.Materia

class MateriaRepository(val dao: MateriaDao) {

    // 1. Obtener todas (Renombrado para coincidir con el DAO)
    val allMaterias: LiveData<List<Materia>> = dao.getAllMaterias()

    // 2. ¡NUEVO! Obtener las recientes (Para el Home)
    fun getRecentOrFrequentlyUsedMaterias(): LiveData<List<Materia>> {
        return dao.getRecentOrFrequentlyUsedMaterias()
    }

    // 3. Insertar (Renombrado)
    suspend fun insertMateria(materia: Materia) = dao.insertMateria(materia)

    // 4. Actualizar (Renombrado)
    suspend fun updateMateria(materia: Materia) = dao.updateMateria(materia)

    // 5. Buscar (Renombrado)
    fun searchMaterias(query: String): LiveData<List<Materia>> {
        return dao.searchMaterias(query)
    }

    // 6. Eliminar (Se mantiene igual)
    suspend fun eliminarMateria(materia: Materia) = dao.eliminarMateria(materia)

}