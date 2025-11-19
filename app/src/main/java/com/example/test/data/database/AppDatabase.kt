package com.example.test.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.test.data.dao.MateriaDao
import com.example.test.data.dao.TareaDao
import com.example.test.data.model.Tarea
import com.example.test.data.model.Materia

@Database(
    entities = [Tarea::class, Materia::class ],
    version = 1
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun tareaDao() : TareaDao
    abstract fun materiaDao(): MateriaDao

}