package com.example.test.data.model

data class TareaConMateria(
    val id: Int,
    val nombreTarea: String,
    val fechaEntrega: String,
    val completada: Boolean,
    val materiaId: Int,
    val materiaNombre: String
)
