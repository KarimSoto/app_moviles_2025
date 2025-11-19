package com.example.test.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


@Entity(
    tableName = "tareas",
    foreignKeys = [ForeignKey(
        entity = Materia::class,
        parentColumns = ["id"],
        childColumns = ["materiaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    val nombreTarea: String,
    val fechaEntrega: String,
    val completada: Boolean,
    val materiaId: Int
)
