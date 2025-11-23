package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog // Importante para el diálogo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.model.Materia
import com.example.test.data.model.Tarea
import com.example.test.data.model.TareaConMateria
import com.example.test.data.repository.MateriaRepository
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TodasTareas : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel
    private lateinit var tareaAdapter: TareaAdapter

    private var currentMaterias: List<Materia> = emptyList()
    private var currentTareas: List<Tarea> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todas_tareas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI
        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareas)
        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)
        val btnAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarTareaTop)
        val textFiltrarTodo = findViewById<TextView>(R.id.textFiltrarTodo)
        val btnFiltroCompletados = findViewById<LinearLayout>(R.id.btnFiltroCompletados)
        val btnFiltroPendientes = findViewById<LinearLayout>(R.id.btnFiltroPendientes)

        // ViewModels
        val db = DatabaseProvider.getDatabase(this)
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(TareaRepository(db.tareaDao()))).get(TareaViewModel::class.java)
        materiaViewModel = ViewModelProvider(this, MateriaViewModelFactory(MateriaRepository(db.materiaDao()))).get(MateriaViewModel::class.java)

        // --- CONFIGURACIÓN DEL ADAPTADOR (CON LAS 3 ACCIONES) ---
        tareaAdapter = TareaAdapter(
            tareas = emptyList(),
            onItemClick = { tarea ->
                // 1. Clic Normal -> Ir a Detalle
                val intent = Intent(this, DetalleTareaActivity::class.java)
                intent.putExtra("id", tarea.id)
                intent.putExtra("nombreTarea", tarea.nombreTarea)
                intent.putExtra("fechaEntrega", tarea.fechaEntrega)
                intent.putExtra("completada", tarea.completada)
                intent.putExtra("materiaId", tarea.materiaId)
                startActivity(intent)
            },
            onEditClick = { tarea ->
                // 2. Clic Editar -> Ir a Editar
                val intent = Intent(this, EditTaskActivity::class.java)
                intent.putExtra("id", tarea.id)
                intent.putExtra("nombreTarea", tarea.nombreTarea)
                intent.putExtra("fechaEntrega", tarea.fechaEntrega)
                intent.putExtra("completada", tarea.completada)
                intent.putExtra("materiaId", tarea.materiaId)
                startActivity(intent)
            },
            onDeleteClick = { tarea ->
                // 3. Clic Eliminar -> Mostrar Alerta
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Tarea")
                    .setMessage("¿Estás seguro de eliminar '${tarea.nombreTarea}'?")
                    .setPositiveButton("Sí") { _, _ ->
                        // Convertir TareaConMateria a Tarea para borrar
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

        // Función para combinar y mostrar
        fun updateList() {
            if (currentMaterias.isEmpty()) return
            val tareasConMateria = currentTareas.map { tarea ->
                val materiaNombre = currentMaterias.find { it.id == tarea.materiaId }?.nombreMateria ?: "Sin materia"
                TareaConMateria(tarea.id, tarea.nombreTarea, tarea.fechaEntrega, tarea.completada, tarea.materiaId, materiaNombre)
            }
            tareaAdapter.updateList(tareasConMateria)
        }

        // Observadores
        materiaViewModel.materias.observe(this) { listaMaterias ->
            currentMaterias = listaMaterias
            updateList()
        }
        tareaViewModel.tareas.observe(this) { listaTareas ->
            currentTareas = listaTareas
            updateList()
        }

        // Listeners
        btnRegresar.setOnClickListener { finish() }
        btnAgregar.setOnClickListener { startActivity(Intent(this, AddTaskActivity::class.java)) }

        textFiltrarTodo.setOnClickListener {
            search.text.clear()
            tareaViewModel.tareas.observe(this) { lista -> currentTareas = lista; updateList() }
            Toast.makeText(this, "Mostrando todas", Toast.LENGTH_SHORT).show()
        }

        btnFiltroCompletados.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(true).observe(this) { lista -> currentTareas = lista; updateList() }
            Toast.makeText(this, "Filtrando: Completadas", Toast.LENGTH_SHORT).show()
        }

        btnFiltroPendientes.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(false).observe(this) { lista -> currentTareas = lista; updateList() }
            Toast.makeText(this, "Filtrando: Pendientes", Toast.LENGTH_SHORT).show()
        }

        lupa.setOnClickListener {
            val texto = search.text.toString().trim()
            if (texto.isNotEmpty()) {
                tareaViewModel.buscarTareas(texto).observe(this) { lista -> currentTareas = lista; updateList() }
            } else textFiltrarTodo.performClick()
        }
    }
}