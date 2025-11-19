package com.example.test.ui.view

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class DetalleMateriaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_materia)

        val nombreMateria = intent.getStringExtra("nombreMateria")
        val idMateria = intent.getIntExtra("id", -1)

        val textView = findViewById<TextView>(R.id.textDetalle)
        textView.text = "ID: $idMateria\nMateria: $nombreMateria"
    }
}
