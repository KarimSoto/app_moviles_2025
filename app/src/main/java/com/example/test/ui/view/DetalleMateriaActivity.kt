package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog // Importante
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.model.TareaConMateria
import com.example.test.data.model.Tarea
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleMateriaActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var tareaAdapter: TareaAdapter

    private var listaCompletaTareas: List<Tarea> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_materia)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val materiaId = intent.getIntExtra("id", -1)
        val nombreMateriaStr = intent.getStringExtra("nombreMateria") ?: "Detalle de Materia"

        val tituloMateria = findViewById<TextView>(R.id.tituloMateriaDetalle)
        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareasMateria)
        val textSinTareas = findViewById<TextView>(R.id.textSinTareas)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresarDetalle)
        val btnAgregarTarea = findViewById<FloatingActionButton>(R.id.fabAgregarTareaMateria)
        val inputBusqueda = findViewById<EditText>(R.id.barra_busqueda)
        val botonBuscar = findViewById<ImageView>(R.id.boton_buscar)
        val btnFiltroCompletados = findViewById<LinearLayout>(R.id.btnFiltroCompletados)
        val btnFiltroPendientes = findViewById<LinearLayout>(R.id.btnFiltroPendientes)

        tituloMateria.text = nombreMateriaStr

        val db = DatabaseProvider.getDatabase(this)
        val tareaRepo = TareaRepository(db.tareaDao())
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(tareaRepo)).get(TareaViewModel::class.java)

        // --- CONFIGURACIÓN DEL ADAPTADOR (CON LAS 3 ACCIONES) ---
        tareaAdapter = TareaAdapter(
            tareas = emptyList(),
            onItemClick = { tarea ->
                val intent = Intent(this, DetalleTareaActivity::class.java)
                intent.putExtra("id", tarea.id)
                intent.putExtra("nombreTarea", tarea.nombreTarea)
                intent.putExtra("fechaEntrega", tarea.fechaEntrega)
                intent.putExtra("completada", tarea.completada)
                intent.putExtra("materiaId", tarea.materiaId)
                startActivity(intent)
            },
            onEditClick = { tarea ->
                val intent = Intent(this, EditTaskActivity::class.java)
                intent.putExtra("id", tarea.id)
                intent.putExtra("nombreTarea", tarea.nombreTarea)
                intent.putExtra("fechaEntrega", tarea.fechaEntrega)
                intent.putExtra("completada", tarea.completada)
                intent.putExtra("materiaId", tarea.materiaId)
                startActivity(intent)
            },
            onDeleteClick = { tarea ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Tarea")
                    .setMessage("¿Eliminar '${tarea.nombreTarea}'?")
                    .setPositiveButton("Sí") { _, _ ->
                        val tareaParaBorrar = Tarea(
                            id = tarea.id,
                            nombreTarea = tarea.nombreTarea,
                            fechaEntrega = tarea.fechaEntrega,
                            completada = tarea.completada,
                            materiaId = tarea.materiaId
                        )
                        tareaViewModel.eliminarTarea(tareaParaBorrar)
                        Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)

        // Función para mostrar
        fun actualizarLista(lista: List<Tarea>) {
            if (lista.isNullOrEmpty()) {
                recyclerTareas.visibility = View.GONE
                textSinTareas.visibility = View.VISIBLE
            } else {
                recyclerTareas.visibility = View.VISIBLE
                textSinTareas.visibility = View.GONE
                val tareasConMateria = lista.map { tarea ->
                    TareaConMateria(tarea.id, tarea.nombreTarea, tarea.fechaEntrega, tarea.completada, tarea.materiaId, nombreMateriaStr)
                }
                tareaAdapter.updateList(tareasConMateria)
            }
        }

        if (materiaId != -1) {
            tareaViewModel.getTareasByMateriaId(materiaId).observe(this) { listaTareas ->
                listaCompletaTareas = listaTareas
                actualizarLista(listaTareas)
            }
        }

        botonBuscar.setOnClickListener {
            val query = inputBusqueda.text.toString().trim().lowercase()
            if (query.isNotEmpty()) {
                actualizarLista(listaCompletaTareas.filter { it.nombreTarea.lowercase().contains(query) })
            } else actualizarLista(listaCompletaTareas)
        }

        btnFiltroCompletados.setOnClickListener {
            actualizarLista(listaCompletaTareas.filter { it.completada })
            Toast.makeText(this, "Mostrando completadas", Toast.LENGTH_SHORT).show()
        }

        btnFiltroPendientes.setOnClickListener {
            actualizarLista(listaCompletaTareas.filter { !it.completada })
            Toast.makeText(this, "Mostrando pendientes", Toast.LENGTH_SHORT).show()
        }

        tituloMateria.setOnClickListener {
            actualizarLista(listaCompletaTareas)
            inputBusqueda.text.clear()
            Toast.makeText(this, "Todas las tareas", Toast.LENGTH_SHORT).show()
        }

        btnAgregarTarea.setOnClickListener { startActivity(Intent(this, AddTaskActivity::class.java)) }
        btnRegresar.setOnClickListener { finish() }
    }
}       