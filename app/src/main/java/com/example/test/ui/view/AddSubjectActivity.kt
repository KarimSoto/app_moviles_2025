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
import com.google.android.material.floatingactionbutton.FloatingActionButton // Importante para el botón de flecha

class AddSubjectActivity : AppCompatActivity() {

    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_subject)

        // 1. Referencias a los NUEVOS IDs del XML
        val inputNombreMateria = findViewById<EditText>(R.id.editTextNombreMateria) // Antes R.id.materia
        val botonRegistrar = findViewById<Button>(R.id.buttonCrearMateria)       // Antes R.id.enviar
        val botonRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar) // Nuevo botón de flecha

        // Ajuste de bordes (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Configuración de Base de Datos y ViewModel (Igual que antes)
        val db = DatabaseProvider.getDatabase(this)
        val materiaDao = db.materiaDao()
        val materiaRepository = MateriaRepository(materiaDao)
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)

        // 3. Observador para verificar si se guardó correctamente
        materiaViewModel.insercionExitosa.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, "Materia registrada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra esta pantalla y regresa automáticamente al menú principal
            } else {
                Toast.makeText(this, "Error al insertar la materia", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Acción del botón "Registrar materia"
        botonRegistrar.setOnClickListener {
            val tituloMateria = inputNombreMateria.text.toString().trim()

            if (tituloMateria.isNotEmpty()) {
                val nuevaMateria = Materia(
                    id = 0,
                    nombreMateria = tituloMateria
                )
                materiaViewModel.agregarMateria(nuevaMateria)
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Acción del botón "Regresar" (Flecha izquierda)
        botonRegresar.setOnClickListener {
            finish() // Cierra la actividad y vuelve atrás
        }
    }
}