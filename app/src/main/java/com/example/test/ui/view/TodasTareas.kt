package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

    // Variables para guardar los datos en memoria y combinarlos
    private var currentMaterias: List<Materia> = emptyList()
    private var currentTareas: List<Tarea> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todas_tareas)

        // Ajuste de bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- REFERENCIAS UI ---
        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareas)
        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)

        // Nuevos botones del diseño actualizado
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)
        val btnAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarTareaTop)
        val textFiltrarTodo = findViewById<TextView>(R.id.textFiltrarTodo)
        val btnFiltroCompletados = findViewById<LinearLayout>(R.id.btnFiltroCompletados)
        val btnFiltroPendientes = findViewById<LinearLayout>(R.id.btnFiltroPendientes)

        // --- CONFIGURACIÓN RECYCLER ---
        tareaAdapter = TareaAdapter(emptyList())
        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)

        // --- VIEWMODELS ---
        val db = DatabaseProvider.getDatabase(this)
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(TareaRepository(db.tareaDao()))).get(TareaViewModel::class.java)
        materiaViewModel = ViewModelProvider(this, MateriaViewModelFactory(MateriaRepository(db.materiaDao()))).get(MateriaViewModel::class.java)

        // --- FUNCIÓN PARA COMBINAR Y MOSTRAR ---
        fun updateList() {
            if (currentMaterias.isEmpty()) return

            val tareasConMateria = currentTareas.map { tarea ->
                val materiaNombre = currentMaterias.find { it.id == tarea.materiaId }?.nombreMateria ?: "Sin materia"
                TareaConMateria(
                    id = tarea.id,
                    nombreTarea = tarea.nombreTarea,
                    fechaEntrega = tarea.fechaEntrega,
                    completada = tarea.completada,
                    materiaId = tarea.materiaId,
                    materiaNombre = materiaNombre
                )
            }
            tareaAdapter.updateList(tareasConMateria)
        }

        // --- OBSERVADORES INICIALES ---

        // 1. Obtener Materias (Una sola vez para tener la referencia de nombres)
        materiaViewModel.materias.observe(this) { listaMaterias ->
            currentMaterias = listaMaterias
            updateList()
        }

        // 2. Obtener Todas las Tareas (Carga inicial)
        tareaViewModel.tareas.observe(this) { listaTareas ->
            currentTareas = listaTareas
            updateList()
        }

        // --- LISTENERS (ACCIONES DE BOTONES) ---

        // 1. BOTÓN REGRESAR (Flecha abajo)
        btnRegresar.setOnClickListener { finish() }

        // 2. BOTÓN AGREGAR (+)
        btnAgregar.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        // 3. FILTRAR TODO (Reset)
        // Vuelve a cargar la lista completa sin filtros
        textFiltrarTodo.setOnClickListener {
            search.text.clear()
            tareaViewModel.tareas.observe(this) { lista ->
                currentTareas = lista
                updateList()
            }
            Toast.makeText(this, "Mostrando todas", Toast.LENGTH_SHORT).show()
        }

        // 4. FILTRO COMPLETADOS
        btnFiltroCompletados.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(true).observe(this) { lista ->
                currentTareas = lista
                updateList()
            }
            Toast.makeText(this, "Filtrando: Completadas", Toast.LENGTH_SHORT).show()
        }

        // 5. FILTRO PENDIENTES
        btnFiltroPendientes.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(false).observe(this) { lista ->
                currentTareas = lista
                updateList()
            }
            Toast.makeText(this, "Filtrando: Pendientes", Toast.LENGTH_SHORT).show()
        }

        // 6. BUSCADOR
        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                tareaViewModel.buscarTareas(textoBusqueda).observe(this) { lista ->
                    currentTareas = lista
                    updateList()
                }
            } else {
                // Si el buscador está vacío, recargamos todas (como el botón Filtrar todo)
                textFiltrarTodo.performClick()
            }
        }
    }
}