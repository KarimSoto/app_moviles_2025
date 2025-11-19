package com.example.test.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.test.data.model.Materia


@Dao
interface MateriaDao {

    @Query("select * from materias")
    fun obtenerMaterias() : LiveData<List<Materia>>

    @Query("select * from materias where nombreMateria like '%' || :nombre || '%'")
    fun buscarMateriasPorNombre(nombre:String): LiveData<List<Materia>>

    @Insert
    suspend fun agregarMateria(materia: Materia)

    @Update
    suspend fun actualizarMateria(materia: Materia)

    @Delete
    suspend fun eliminarMateria(materia: Materia)

}