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

class EditSubjectActivity : AppCompatActivity() {

    private lateinit var materiaViewModel: MateriaViewModel
    private var materiaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_subject)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Recibir Datos
        materiaId = intent.getIntExtra("id", -1)
        val nombreActual = intent.getStringExtra("nombreMateria") ?: ""

        // 2. Referencias UI
        val editTextNombre = findViewById<EditText>(R.id.editTextNombreMateria)
        val btnGuardar = findViewById<Button>(R.id.buttonGuardarCambios)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)

        // Pre-llenar el nombre
        editTextNombre.setText(nombreActual)

        // 3. Configurar ViewModel
        val db = DatabaseProvider.getDatabase(this)
        val repo = MateriaRepository(db.materiaDao())
        materiaViewModel = ViewModelProvider(this, MateriaViewModelFactory(repo)).get(MateriaViewModel::class.java)

        // 4. Botón Guardar
        btnGuardar.setOnClickListener {
            val nuevoNombre = editTextNombre.text.toString().trim()

            if (nuevoNombre.isNotEmpty() && materiaId != -1) {
                // Creamos el objeto materia con el MISMO ID pero NUEVO nombre
                // (Esto hará que Room actualice la fila existente en lugar de crear una nueva)
                val materiaActualizada = Materia(
                    id = materiaId,
                    nombreMateria = nuevoNombre
                    // ultimaInteraccion se mantiene o se actualiza si quieres,
                    // por defecto en el modelo suele tener un valor, pero al hacer update
                    // Room respetará el ID.
                )

                materiaViewModel.actualizarMateria(materiaActualizada)

                Toast.makeText(this, "Materia actualizada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Regresamos
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Botón Regresar
        btnRegresar.setOnClickListener { finish() }
    }
}