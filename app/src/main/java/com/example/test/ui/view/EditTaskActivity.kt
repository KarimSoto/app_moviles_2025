package com.example.test.ui.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.model.Materia
import com.example.test.data.model.Tarea
import com.example.test.data.repository.MateriaRepository
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTaskActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel

    private var selectedMateriaId: Int = -1
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_tarea)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Inicialización de DB y ViewModels ---
        val database = DatabaseProvider.getDatabase(this)
        val tareaRepository = TareaRepository(database.tareaDao())
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(tareaRepository)).get(TareaViewModel::class.java)
        val materiaRepository = MateriaRepository(database.materiaDao())
        materiaViewModel = ViewModelProvider(this, MateriaViewModelFactory(materiaRepository)).get(MateriaViewModel::class.java)

        // --- Recibir datos ---
        val id = intent.getIntExtra("id", -1)
        val nombreTarea = intent.getStringExtra("nombreTarea") ?: ""
        val fechaEntrega = intent.getStringExtra("fechaEntrega") ?: ""
        val completada = intent.getBooleanExtra("completada", false)
        // La materia actual que viene desde el detalle
        selectedMateriaId = intent.getIntExtra("materiaId", -1)

        // --- Referencias UI ---
        val etTitulo = findViewById<TextInputEditText>(R.id.editTextTitulo)
        val etFecha = findViewById<TextInputEditText>(R.id.editTextFecha)
        val spinnerMaterias = findViewById<AutoCompleteTextView>(R.id.spinnerMaterias)
        val btnGuardar = findViewById<Button>(R.id.botonGuardar)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)

        // --- Pre-llenar datos ---
        etTitulo.setText(nombreTarea)
        etFecha.setText(fechaEntrega)

        // --- Configurar Spinner de Materias ---
        var materiasList: List<Materia> = emptyList()
        materiaViewModel.materias.observe(this) { lista ->
            materiasList = lista
            val nombres = lista.map { it.nombreMateria }

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
            spinnerMaterias.setAdapter(adapter)

            // Buscar la materia actual para pre-seleccionarla en el texto
            val materiaActual = lista.find { it.id == selectedMateriaId }
            if (materiaActual != null) {
                // setText(nombre, false) -> El false es importante para que no filtre la lista al iniciar
                spinnerMaterias.setText(materiaActual.nombreMateria, false)
            }
        }

        // Listener de selección de materia
        spinnerMaterias.setOnItemClickListener { _, _, position, _ ->
            selectedMateriaId = materiasList[position].id
        }

        // --- Configurar Calendario ---
        etFecha.setOnClickListener {
            // Parsear fecha actual del campo si existe para iniciar el calendario ahí
            try {
                val partes = etFecha.text.toString().split("-")
                if (partes.size == 3) {
                    calendar.set(Calendar.YEAR, partes[0].toInt())
                    calendar.set(Calendar.MONTH, partes[1].toInt() - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, partes[2].toInt())
                }
            } catch (e: Exception) { /* Si falla, usa fecha actual */ }

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    etFecha.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // --- Guardar Cambios ---
        btnGuardar.setOnClickListener {
            val nuevoNombre = etTitulo.text.toString().trim()
            val nuevaFecha = etFecha.text.toString().trim()

            if (nuevoNombre.isEmpty()) {
                etTitulo.error = "El título es obligatorio"
                return@setOnClickListener
            }
            if (selectedMateriaId == -1) {
                spinnerMaterias.error = "Selecciona una materia válida"
                return@setOnClickListener
            }

            val tareaActualizada = Tarea(
                id = id,
                nombreTarea = nuevoNombre,
                fechaEntrega = nuevaFecha,
                completada = completada,
                materiaId = selectedMateriaId
                // Si tienes campo descripción en tu modelo, agrégalo aquí
            )

            tareaViewModel.actualizarTarea(tareaActualizada)

            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish() // Regresa a la pantalla anterior
        }

        // --- Botón Regresar ---
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}