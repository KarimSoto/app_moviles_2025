package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
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

class MainActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel
    private lateinit var tareaAdapter: TareaAdapter
    private lateinit var materiaAdapter: MateriaAdapter

    // Variables para guardar los datos actuales y poder combinarlos
    private var currentTareas: List<Tarea> = emptyList()
    private var currentMaterias: List<Materia> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- Initialization (ViewModels, DB, UI Elements, Adapters) ---
        // (Este bloque es igual que antes, solo lo resumo aquí)
        val db = DatabaseProvider.getDatabase(this)
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(TareaRepository(db.tareaDao()))).get(TareaViewModel::class.java)
        materiaViewModel = ViewModelProvider(this, MateriaViewModelFactory(MateriaRepository(db.materiaDao()))).get(MateriaViewModel::class.java)

        val recyclerMaterias = findViewById<RecyclerView>(R.id.recyclerMaterias)
        materiaAdapter = MateriaAdapter(emptyList()) { materiaClicked ->
            materiaViewModel.updateMateriaInteraction(materiaClicked)
            val intent = Intent(this, DetalleMateriaActivity::class.java)
            intent.putExtra("nombreMateria", materiaClicked.nombreMateria)
            intent.putExtra("id", materiaClicked.id)
            startActivity(intent)
        }
        recyclerMaterias.adapter = materiaAdapter
        recyclerMaterias.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recyclerMaterias.isNestedScrollingEnabled = false

        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareas)
        tareaAdapter = TareaAdapter(emptyList())
        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)
        recyclerTareas.isNestedScrollingEnabled = false

        // --- Función para combinar Tareas y Materias ---
        // Esta es la clave de la corrección.
        fun updateTareasList() {
            if (currentMaterias.isEmpty()) return // Si no hay materias, no podemos combinar

            // Tomamos las tareas (limitadas a 10 si es la vista principal)
            // Nota: La limitación se aplica antes de llamar a esta función en los casos de búsqueda/filtro
            val listaLimitada = if (currentTareas.size > 10) currentTareas.take(10) else currentTareas

            val tareasConMateria = listaLimitada.map { tarea ->
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

        // --- Observadores Corregidos (Independientes) ---

        // 1. Observar Materias Recientes (Grid superior)
        materiaViewModel.recentMaterias.observe(this) { lista ->
            materiaAdapter.updateList(lista)
        }

        // 2. Observar TODAS las Materias (Para poder combinar nombres)
        materiaViewModel.materias.observe(this) { listaMaterias ->
            currentMaterias = listaMaterias
            updateTareasList() // Intentar actualizar la lista cada vez que lleguen materias
        }

        // 3. Observar Tareas (Lista inferior)
        tareaViewModel.tareas.observe(this) { listaTareas ->
            currentTareas = listaTareas
            updateTareasList() // Intentar actualizar la lista cada vez que lleguen tareas
        }


        // --- Lógica de Botones y Búsqueda (Actualizada para usar updateTareasList) ---
        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)
        val home = findViewById<Button>(R.id.home)
        val botonCompletados = findViewById<Button>(R.id.ButtonCompletados)
        val botonPendientes = findViewById<Button>(R.id.ButtonPendientes)

        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                materiaViewModel.buscarMaterias(textoBusqueda).observe(this) { lista -> materiaAdapter.updateList(lista) }
                // Al buscar, actualizamos currentTareas con el resultado y llamamos a la función
                tareaViewModel.buscarTareas(textoBusqueda).observe(this) { lista ->
                    currentTareas = lista
                    updateTareasList()
                }
            } else {
                home.performClick() // Si limpian, volver al estado inicial
            }
        }

        home.setOnClickListener {
            search.text.clear(); search.clearFocus()
            // Restaurar observadores originales
            materiaViewModel.recentMaterias.observe(this) { lista -> materiaAdapter.updateList(lista) }
            tareaViewModel.tareas.observe(this) { lista ->
                currentTareas = lista
                updateTareasList()
            }
        }

        botonCompletados.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(true).observe(this) { lista ->
                currentTareas = lista
                updateTareasList()
            }
        }

        botonPendientes.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(false).observe(this) { lista ->
                currentTareas = lista
                updateTareasList()
            }
        }

        // --- Navigation Buttons & Insets (Igual que antes) ---
        // ...
        val todasMaterias = findViewById<TextView>(R.id.buttonVerTodo)
        val todasTareas = findViewById<TextView>(R.id.buttonVerTodasTareas)
        val addSubject = findViewById<FloatingActionButton>(R.id.redireccionarMateria)
        val addTask = findViewById<Button>(R.id.redireccionarTarea)

        todasMaterias.setOnClickListener { startActivity(Intent(this, TodasMaterias::class.java)) }
        todasTareas.setOnClickListener { startActivity(Intent(this, TodasTareas::class.java)) }
        addTask.setOnClickListener { startActivity(Intent(this, AddTaskActivity::class.java)) }
        addSubject.setOnClickListener { startActivity(Intent(this, AddSubjectActivity::class.java)) }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}