package com.example.test.ui.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.repository.TareaRepository
import com.example.test.data.repository.MateriaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory
import java.time.LocalDate

class EditTaskActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_tarea)

        // Inicializamos base de datos y viewmodels
        val database = DatabaseProvider.getDatabase(this)

        val tareaRepository = TareaRepository(database.tareaDao())
        val tareaFactory = TareaViewModelFactory(tareaRepository)
        tareaViewModel = ViewModelProvider(this, tareaFactory)[TareaViewModel::class.java]

        val materiaRepository = MateriaRepository(database.materiaDao())
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory)[MateriaViewModel::class.java]

        // Recibimos los datos
        val id = intent.getIntExtra("id", -1)
        val nombreTarea = intent.getStringExtra("nombreTarea")
        val fechaEntrega = intent.getStringExtra("fechaEntrega")
        val materiaId = intent.getIntExtra("materiaId", -1)

        // Referencias UI
        val inputTarea = findViewById<EditText>(R.id.titulo)
        val calendarFecha = findViewById<DatePicker>(R.id.fecha)
        val spinnerMateria = findViewById<Spinner>(R.id.materia)
        val botonGuardar = findViewById<Button>(R.id.botonGuardar)

        // Mostrar nombre actual como hint
        inputTarea.hint = "Tarea: $nombreTarea"

        // Configurar fecha en el DatePicker
        if (!fechaEntrega.isNullOrEmpty()) {
            val partes = fechaEntrega.split("-")
            if (partes.size == 3) {
                val year = partes[0].toInt()
                val month = partes[1].toInt() - 1 // DatePicker usa meses 0-11
                val day = partes[2].toInt()
                calendarFecha.updateDate(year, month, day)
            }
        }

        // --- Spinner de materias ---
        materiaViewModel.materias.observe(this) { lista ->
            val nombres = mutableListOf("Selecciona una materia...") // opción inicial
            nombres.addAll(lista.map { it.nombreMateria })

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMateria.adapter = adapter

            // Buscar índice de materia actual y marcarla (si existe)
            val materiaActual = lista.find { it.id == materiaId }
            val index = if (materiaActual != null) nombres.indexOf(materiaActual.nombreMateria) else 0
            spinnerMateria.setSelection(index)
        }

        // --- Botón Guardar ---
        botonGuardar.setOnClickListener {
            val nuevoNombre = inputTarea.text.toString().ifBlank { nombreTarea ?: "" }

            val fechaSeleccionada = LocalDate.of(
                calendarFecha.year,
                calendarFecha.month + 1,
                calendarFecha.dayOfMonth
            ).toString()

            val seleccion = spinnerMateria.selectedItemPosition
            val nuevoMateriaId =
                if (seleccion == 0) materiaId // no cambió materia
                else materiaViewModel.materias.value?.get(seleccion - 1)?.id ?: materiaId

            val tareaActualizada = com.example.test.data.model.Tarea(
                id = id,
                nombreTarea = nuevoNombre,
                fechaEntrega = fechaSeleccionada,
                completada = false, // o pon el valor real si lo manejas en otro lado
                materiaId = nuevoMateriaId
            )

            // Llamar al viewModel para actualizar tarea
            tareaViewModel.actualizarTarea(tareaActualizada)

            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
            finish() // volver al detalle o lista
        }
    }
}
