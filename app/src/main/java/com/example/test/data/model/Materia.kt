package com.example.test.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate=true) val id: Int = 0,
    val nombreMateria: String
)
