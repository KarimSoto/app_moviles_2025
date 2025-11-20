package com.example.test.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.test.data.model.Materia

@Dao
interface MateriaDao {

    // 1. Obtener TODAS las materias (Ordenadas por ID descendente para ver las nuevas primero en la lista completa)
    // Renombrado de 'obtenerMaterias' a 'getAllMaterias' para coincidir con el Repository nuevo
    @Query("SELECT * FROM materias ORDER BY id DESC")
    fun getAllMaterias(): LiveData<List<Materia>>

    // 2. Buscar materias (Ordenadas por las que usaste más recientemente)
    // Renombrado de 'buscarMateriasPorNombre' a 'searchMaterias'
    @Query("SELECT * FROM materias WHERE nombreMateria LIKE '%' || :query || '%' ORDER BY ultimaInteraccion DESC")
    fun searchMaterias(query: String): LiveData<List<Materia>>

    // 3. ¡NUEVO! Obtener solo las 4 materias más recientes (Para el Home)
    @Query("SELECT * FROM materias ORDER BY ultimaInteraccion DESC LIMIT 4")
    fun getRecentOrFrequentlyUsedMaterias(): LiveData<List<Materia>>

    // 4. Insertar materia
    // Renombrado de 'agregarMateria' a 'insertMateria'
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMateria(materia: Materia)

    // 5. Actualizar materia (Usado para guardar la fecha de última interacción)
    // Renombrado de 'actualizarMateria' a 'updateMateria'
    @Update
    suspend fun updateMateria(materia: Materia)

    // 6. Eliminar materia (Mantenemos el nombre original por si se usa en otro lado)
    @Delete
    suspend fun eliminarMateria(materia: Materia)

}