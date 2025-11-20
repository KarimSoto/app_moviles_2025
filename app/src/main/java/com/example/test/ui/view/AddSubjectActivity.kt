package com.example.test.ui.view

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.model.Materia
import com.example.test.data.repository.MateriaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddSubjectActivity : AppCompatActivity() {

    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_subject)

        // Referencias a los IDs
        val inputNombreMateria = findViewById<EditText>(R.id.editTextNombreMateria)
        val botonRegistrar = findViewById<Button>(R.id.buttonCrearMateria)
        val botonRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)

        // Ajuste de bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración ViewModel
        val db = DatabaseProvider.getDatabase(this)
        val materiaDao = db.materiaDao()
        val materiaRepository = MateriaRepository(materiaDao)
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)

        // --- OBSERVADOR DE ÉXITO ---
        // Aquí es donde se "escucha" si la operación funcionó para mostrar el mensaje y salir
        materiaViewModel.insercionExitosa.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Materia registrada con éxito", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad y regresa a la pantalla principal
            } else {
                Toast.makeText(this, "Error al guardar la materia", Toast.LENGTH_SHORT).show()
            }
        }

        // --- BOTÓN REGISTRAR ---
        botonRegistrar.setOnClickListener {
            val tituloMateria = inputNombreMateria.text.toString().trim()

            if (tituloMateria.isNotEmpty()) {
                val nuevaMateria = Materia(
                    id = 0,
                    nombreMateria = tituloMateria
                )

                // ¡CORRECCIÓN AQUÍ! Usamos insertMateria en lugar de agregarMateria
                // Esta función sí dispara el aviso 'insercionExitosa'
                materiaViewModel.insertMateria(nuevaMateria)

            } else {
                Toast.makeText(this, "Por favor escribe un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        // --- BOTÓN REGRESAR ---
        botonRegresar.setOnClickListener {
            finish()
        }
    }
}