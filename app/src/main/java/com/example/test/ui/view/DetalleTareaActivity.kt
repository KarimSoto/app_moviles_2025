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

class DetalleTareaActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_tarea)

        val database = DatabaseProvider.getDatabase(this)
        val repository = TareaRepository(database.tareaDao())
        val factory = TareaViewModelFactory(repository)
        tareaViewModel = ViewModelProvider(this, factory).get(TareaViewModel::class.java)

        val radioTardio = findViewById<RadioButton>(R.id.radioTardio)
        val radioProceso = findViewById<RadioButton>(R.id.radioProceso)
        val radioCompletado = findViewById<RadioButton>(R.id.radioCompletado)
        val tareaEntregada = findViewById<RadioButton>(R.id.tareaEntregada)
        val tareaNoEntregada = findViewById<RadioButton>(R.id.tareaNoEntregada)

        val id = intent.getIntExtra("id", -1)
        val nombreTarea = intent.getStringExtra("nombreTarea")
        val fechaEntrega = intent.getStringExtra("fechaEntrega")
        val completada = intent.getBooleanExtra("completada", false)
        val materiaId = intent.getIntExtra("materiaId", -1)

        val textTarea = findViewById<TextView>(R.id.nombreTarea)
        textTarea.text = "Tarea: $nombreTarea"

        val textfechaEntrega = findViewById<TextView>(R.id.fechaEntrega)
        textfechaEntrega.text = "Fecha de entrega: $fechaEntrega"

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaTarea = LocalDate.parse(fechaEntrega, formatter)
        val hoy = LocalDate.now()

        if (completada) {
            tareaEntregada.isChecked = true
            radioCompletado.isChecked = true
        } else {
            tareaNoEntregada.isChecked = true
            if (fechaTarea.isBefore(hoy)) radioTardio.isChecked = true
            else radioProceso.isChecked = true
        }





        // Los radios de estado no se pueden editar directamente
        if(radioCompletado.isChecked) {
            radioTardio.isEnabled = false
            radioProceso.isEnabled = false
        }

        if(radioTardio.isChecked){
            radioCompletado.isEnabled = false
            radioProceso.isEnabled = false
        }

        if(radioProceso.isChecked){
            radioTardio.isEnabled = false
            radioCompletado.isEnabled = false
        }

        // --- Mostrar nombre de materia ---
        val textnombreMateria = findViewById<TextView>(R.id.nombreMateria)
        if (materiaId != -1) {
            tareaViewModel.buscarMateria(materiaId).observe(this) { materia ->
                textnombreMateria.text = materia.nombreMateria
            }
        }

        // --- Botón editar tarea ---
        val botonEditarTarea = findViewById<Button>(R.id.botonEditar)
        botonEditarTarea.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("nombreTarea", nombreTarea)
            intent.putExtra("fechaEntrega", fechaEntrega)
            intent.putExtra("completada", completada)
            intent.putExtra("materiaId", materiaId)
            startActivity(intent)
        }

        // --- Listeners para cambiar estado ---
        tareaEntregada.setOnCheckedChangeListener { _, isChecked ->
            if (id != -1 && isChecked) {
                tareaViewModel.cambiarEstadoTarea(id, true)
            }
        }

        tareaNoEntregada.setOnCheckedChangeListener { _, isChecked ->
            if (id != -1 && isChecked) {
                tareaViewModel.cambiarEstadoTarea(id, false)
            }
        }

        // --- Observar resultado del cambio ---
        tareaViewModel.queryExitoso.observe(this) { exito ->
            // Desactivar listeners mientras se actualiza la interfaz
            tareaEntregada.setOnCheckedChangeListener(null)
            tareaNoEntregada.setOnCheckedChangeListener(null)

            radioTardio.isEnabled = true
            radioCompletado.isEnabled = true
            radioProceso.isEnabled = true


            if (exito) {
                if (tareaEntregada.isChecked) {
                    radioCompletado.isChecked = true
                    Toast.makeText(this, "Tarea entregada", Toast.LENGTH_SHORT).show()
                } else {
                    if (fechaTarea.isBefore(hoy)) radioTardio.isChecked = true
                    else radioProceso.isChecked = true
                    Toast.makeText(this, "Tarea no entregada", Toast.LENGTH_SHORT).show()
                }
            }

            else {
                // Si falló, revertimos el estado visual
                tareaEntregada.isChecked = !tareaEntregada.isChecked
                tareaNoEntregada.isChecked = !tareaNoEntregada.isChecked
                Toast.makeText(this, "Hubo un error, vuelve a intentarlo", Toast.LENGTH_SHORT).show()
            }

            if(radioCompletado.isChecked) {
                radioTardio.isEnabled = false
                radioProceso.isEnabled = false
            }

            if(radioTardio.isChecked){
                radioCompletado.isEnabled = false
                radioProceso.isEnabled = false
            }

            if(radioProceso.isChecked){
                radioTardio.isEnabled = false
                radioCompletado.isEnabled = false
            }

            // Restaurar listeners
            tareaEntregada.setOnCheckedChangeListener { _, isChecked ->
                if (id != -1 && isChecked) tareaViewModel.cambiarEstadoTarea(id, true)
            }

            tareaNoEntregada.setOnCheckedChangeListener { _, isChecked ->
                if (id != -1 && isChecked) tareaViewModel.cambiarEstadoTarea(id, false)
            }
        }
    }
}
