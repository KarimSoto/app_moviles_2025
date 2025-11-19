package com.example.test.data.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.test.data.model.Tarea
import com.example.test.data.model.Materia

@Dao
interface TareaDao {

    @Query("select * from tareas")
    fun obtenerTareas(): LiveData<List<Tarea>>



    @Query("select * from materias where id = :id")
    fun buscarMateriaPorId(id: Int): LiveData<Materia>


    @Query("select * from tareas where nombreTarea like '%' || :nombre || '%'")
    fun buscarTareasPorNombre(nombre:String): LiveData<List<Tarea>>


    @Query("select * from tareas where completada = :estado")
    fun buscarTareasPorEstado(estado: Boolean): LiveData<List<Tarea>>


    @Query("update tareas set completada = :estado where id = :id")
    suspend fun cambiarEstadoTarea(id: Int, estado:Boolean)

    @Insert
    suspend fun agregarTarea(tarea: Tarea)

    @Update
    suspend fun actualizarTarea(tarea: Tarea)

    @Delete
    suspend fun eliminarTarea(tarea: Tarea)

}