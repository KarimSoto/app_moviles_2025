package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleTareaActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_tarea)

        val database = DatabaseProvider.getDatabase(this)
        val repository = TareaRepository(database.tareaDao())
        val factory = TareaViewModelFactory(repository)
        tareaViewModel = ViewModelProvider(this, factory).get(TareaViewModel::class.java)

        // Referencias UI (Nuevos IDs del XML rediseñado)
        val textTarea = findViewById<TextView>(R.id.nombreTarea)
        val textFecha = findViewById<TextView>(R.id.fechaEntrega)
        val textDescripcion = findViewById<TextView>(R.id.descripcion)
        val textMateria = findViewById<TextView>(R.id.nombreMateria)

        val radioGroupEntrega = findViewById<RadioGroup>(R.id.grupoEntrega)
        val radioGroupEstado = findViewById<RadioGroup>(R.id.grupoEstado)

        val radioEntregada = findViewById<RadioButton>(R.id.tareaEntregada)
        val radioNoEntregada = findViewById<RadioButton>(R.id.tareaNoEntregada)

        val radioTardio = findViewById<RadioButton>(R.id.radioTardio)
        val radioProceso = findViewById<RadioButton>(R.id.radioProceso)
        val radioCompletado = findViewById<RadioButton>(R.id.radioCompletado)

        val botonEditar = findViewById<Button>(R.id.botonEditar)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)

        // Obtener datos del Intent
        val id = intent.getIntExtra("id", -1)
        val nombreTarea = intent.getStringExtra("nombreTarea")
        val fechaEntrega = intent.getStringExtra("fechaEntrega")
        val completada = intent.getBooleanExtra("completada", false)
        val materiaId = intent.getIntExtra("materiaId", -1)
        // Si pasaste descripción en el intent, úsala; si no, placeholder
        val descripcion = intent.getStringExtra("descripcion") ?: "Sin descripción adicional."

        // Setear textos
        textTarea.text = nombreTarea
        textFecha.text = fechaEntrega
        textDescripcion.text = descripcion

        // Obtener nombre de materia
        if (materiaId != -1) {
            tareaViewModel.buscarMateria(materiaId).observe(this) { materia ->
                if (materia != null) {
                    textMateria.text = "Materia: ${materia.nombreMateria}"
                } else {
                    textMateria.text = "Materia: Desconocida"
                }
            }
        }

        // Lógica de Fechas
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaTareaDate = try {
            LocalDate.parse(fechaEntrega, formatter)
        } catch (e: Exception) { LocalDate.now() }
        val hoy = LocalDate.now()

        // Configurar estado inicial de RadioButtons
        if (completada) {
            radioEntregada.isChecked = true
            radioCompletado.isChecked = true
        } else {
            radioNoEntregada.isChecked = true
            if (fechaTareaDate.isBefore(hoy)) {
                radioTardio.isChecked = true
            } else {
                radioProceso.isChecked = true
            }
        }

        // Lógica de bloqueo visual (Si está completado, no dejar cambiar estado manual)
        fun actualizarBloqueos() {
            val esCompletado = radioCompletado.isChecked
            radioTardio.isEnabled = !esCompletado
            radioProceso.isEnabled = !esCompletado
        }
        actualizarBloqueos()

        // --- LISTENERS ---

        // 1. Cambio en "Entregada / No entregada"
        radioGroupEntrega.setOnCheckedChangeListener { _, checkedId ->
            if (id != -1) {
                val nuevoEstado = (checkedId == R.id.tareaEntregada)

                // Actualizar en BD
                tareaViewModel.cambiarEstadoTarea(id, nuevoEstado)

                // Actualizar UI local
                if (nuevoEstado) {
                    radioCompletado.isChecked = true
                    Toast.makeText(this, "¡Tarea completada!", Toast.LENGTH_SHORT).show()
                } else {
                    // Si desmarca entregada, recalculamos si es tardío o proceso
                    if (fechaTareaDate.isBefore(hoy)) radioTardio.isChecked = true
                    else radioProceso.isChecked = true
                    Toast.makeText(this, "Tarea marcada como pendiente", Toast.LENGTH_SHORT).show()
                }
                actualizarBloqueos()
            }
        }

        // 2. Botón Editar
        botonEditar.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nombreTarea", nombreTarea)
            intent.putExtra("fechaEntrega", fechaEntrega)
            intent.putExtra("completada", radioEntregada.isChecked)
            intent.putExtra("materiaId", materiaId)
            startActivity(intent)
            // Podríamos llamar finish() si queremos recargar al volver,
            // pero EditTaskActivity debería manejar el retorno.
        }

        // 3. Botón Regresar
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}