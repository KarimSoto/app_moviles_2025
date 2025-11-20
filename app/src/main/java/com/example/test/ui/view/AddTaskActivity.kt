package com.example.test.ui.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

class AddTaskActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel

    // Variables para guardar la selección actual
    private var selectedMateriaId: Int? = null
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)

        // Ajuste de bordes (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 1. Referencias a los elementos del NUEVO diseño ---
        val etNombreTarea = findViewById<TextInputEditText>(R.id.editTextNombreTarea)
        val etDescripcion = findViewById<TextInputEditText>(R.id.editTextDescripcionTarea)
        val etFechaInicio = findViewById<TextInputEditText>(R.id.editTextFechaInicio) // Opcional, no se guarda en BD por ahora
        val etFechaFin = findViewById<TextInputEditText>(R.id.editTextFechaFin)       // Esta será la fecha de entrega
        val spinnerMaterias = findViewById<AutoCompleteTextView>(R.id.spinnerMaterias)
        val btnAgregar = findViewById<Button>(R.id.buttonAgregarTarea)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)

        // --- 2. Configuración de ViewModels y DB ---
        val db = DatabaseProvider.getDatabase(this)

        val tareaRepo = TareaRepository(db.tareaDao())
        val tareaFactory = TareaViewModelFactory(tareaRepo)
        tareaViewModel = ViewModelProvider(this, tareaFactory).get(TareaViewModel::class.java)

        val materiaRepo = MateriaRepository(db.materiaDao())
        val materiaFactory = MateriaViewModelFactory(materiaRepo)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)

        // --- 3. Configurar el Dropdown de Materias ---
        var materiasList: List<Materia> = emptyList()

        // Observamos las materias para llenar el menú
        materiaViewModel.materias.observe(this) { materias ->
            materiasList = materias
            val nombresMaterias = materias.map { it.nombreMateria }

            // Adaptador para el menú desplegable
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresMaterias)
            spinnerMaterias.setAdapter(adapter)
        }

        // Manejamos la selección del dropdown
        spinnerMaterias.setOnItemClickListener { _, _, position, _ ->
            // Al hacer clic en una opción, guardamos el ID de esa materia
            selectedMateriaId = materiasList[position].id
        }

        // --- 4. Configurar Selectores de Fecha (Calendario) ---
        // Función auxiliar para mostrar el calendario y poner la fecha en el campo
        fun showDatePicker(view: TextInputEditText) {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Formato de fecha: 2025-10-25 (Para que sea fácil de ordenar si quieres, o dd/MM/yyyy)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                view.setText(format.format(calendar.time))
            }

            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Al hacer clic en los campos de fecha
        etFechaInicio.setOnClickListener { showDatePicker(etFechaInicio) }
        etFechaFin.setOnClickListener { showDatePicker(etFechaFin) }

        // --- 5. Observador de Éxito (Para saber si se guardó) ---
        tareaViewModel.insercionExitosa.observe(this) { exito ->
            if(exito){
                Toast.makeText(this, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cerramos la pantalla
            } else {
                Toast.makeText(this, "Error al guardar la tarea", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 6. Botón Agregar Tarea ---
        btnAgregar.setOnClickListener {
            val nombre = etNombreTarea.text.toString().trim()
            val fechaEntrega = etFechaFin.text.toString().trim() // Usamos fecha fin como entrega

            // Validaciones
            if (nombre.isEmpty()) {
                etNombreTarea.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (fechaEntrega.isEmpty()) {
                etFechaFin.error = "La fecha de entrega es obligatoria"
                Toast.makeText(this, "Selecciona una fecha fin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedMateriaId == null) {
                spinnerMaterias.error = "Debes seleccionar una materia"
                Toast.makeText(this, "Selecciona una materia de la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Creamos la nueva tarea
            val nuevaTarea = Tarea(
                id = 0,
                nombreTarea = nombre,
                fechaEntrega = fechaEntrega,
                completada = false,
                materiaId = selectedMateriaId!!
                // Nota: Si agregaste un campo 'descripcion' a tu modelo Tarea, agrégalo aquí.
                // Si no, la descripción del formulario es solo visual por ahora.
            )

            // Guardamos en la BD
            tareaViewModel.agregarTarea(nuevaTarea)
        }

        // --- 7. Botón Regresar ---
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}