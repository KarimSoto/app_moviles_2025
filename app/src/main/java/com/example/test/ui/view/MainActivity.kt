package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog // Importante para los pop-ups
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

    // Variables para guardar los datos actuales y poder combinarlos/validarlos
    private var currentTareas: List<Tarea> = emptyList()
    private var currentMaterias: List<Materia> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- Inicialización (ViewModels, DB) ---
        val db = DatabaseProvider.getDatabase(this)
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(TareaRepository(db.tareaDao()))).get(TareaViewModel::class.java)
        materiaViewModel = ViewModelProvider(this, MateriaViewModelFactory(MateriaRepository(db.materiaDao()))).get(MateriaViewModel::class.java)

        // --- Configuración RecyclerViews ---

        // 1. Materias (Grid)
        val recyclerMaterias = findViewById<RecyclerView>(R.id.recyclerMaterias)
        materiaAdapter = MateriaAdapter(
            materias = emptyList(),
            onItemClick = { materiaClicked ->
                materiaViewModel.updateMateriaInteraction(materiaClicked)
                val intent = Intent(this, DetalleMateriaActivity::class.java)
                intent.putExtra("nombreMateria", materiaClicked.nombreMateria)
                intent.putExtra("id", materiaClicked.id)
                startActivity(intent)
            },
            onEditClick = { materia ->
                val intent = Intent(this, EditSubjectActivity::class.java)
                intent.putExtra("id", materia.id)
                intent.putExtra("nombreMateria", materia.nombreMateria)
                startActivity(intent)
            },
            onDeleteClick = { materia ->
                mostrarConfirmacionBorrarMateria(materia)
            }
        )
        recyclerMaterias.adapter = materiaAdapter
        recyclerMaterias.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recyclerMaterias.isNestedScrollingEnabled = false

        // 2. Tareas (Lista)
        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareas)
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
                mostrarConfirmacionBorrarTarea(tarea.nombreTarea, tarea.id, tarea.fechaEntrega, tarea.completada, tarea.materiaId)
            }
        )
        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)
        recyclerTareas.isNestedScrollingEnabled = false

        // --- Función para combinar listas ---
        fun updateTareasList() {
            if (currentMaterias.isEmpty()) return

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

        // --- Observadores ---
        materiaViewModel.recentMaterias.observe(this) { lista ->
            materiaAdapter.updateList(lista)
        }

        materiaViewModel.materias.observe(this) { listaMaterias ->
            currentMaterias = listaMaterias
            updateTareasList()
        }

        tareaViewModel.tareas.observe(this) { listaTareas ->
            currentTareas = listaTareas
            updateTareasList()
        }

        // --- Elementos UI ---
        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)
        val home = findViewById<Button>(R.id.home)
        val botonCompletados = findViewById<Button>(R.id.ButtonCompletados)
        val botonPendientes = findViewById<Button>(R.id.ButtonPendientes)
        val todasMaterias = findViewById<TextView>(R.id.buttonVerTodo)
        val todasTareas = findViewById<TextView>(R.id.buttonVerTodasTareas)

        // BOTÓN FLOTANTE PRINCIPAL (+)
        val fabPrincipal = findViewById<FloatingActionButton>(R.id.redireccionarMateria)

        val addTaskButton = findViewById<Button>(R.id.redireccionarTarea)

        // --- Listeners ---

        // 1. Lógica del FAB Central (+)
        fabPrincipal.setOnClickListener {
            mostrarDialogoSeleccion()
        }

        // 2. Otros botones
        addTaskButton.setOnClickListener {
            // Este botón pequeño en la tarjeta azul también valida antes de ir
            validarYRedireccionarTarea()
        }

        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                materiaViewModel.buscarMaterias(textoBusqueda).observe(this) { lista -> materiaAdapter.updateList(lista) }
                tareaViewModel.buscarTareas(textoBusqueda).observe(this) { lista ->
                    currentTareas = lista
                    updateTareasList()
                }
            } else home.performClick()
        }

        home.setOnClickListener {
            search.text.clear(); search.clearFocus()
            materiaViewModel.recentMaterias.observe(this) { lista -> materiaAdapter.updateList(lista) }
            tareaViewModel.tareas.observe(this) { lista ->
                currentTareas = lista
                updateTareasList()
            }
        }

        botonCompletados.setOnClickListener { tareaViewModel.buscarTareasPorEstado(true).observe(this) { l -> currentTareas = l; updateTareasList() } }
        botonPendientes.setOnClickListener { tareaViewModel.buscarTareasPorEstado(false).observe(this) { l -> currentTareas = l; updateTareasList() } }

        todasMaterias.setOnClickListener { startActivity(Intent(this, TodasMaterias::class.java)) }
        todasTareas.setOnClickListener { startActivity(Intent(this, TodasTareas::class.java)) }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- FUNCIONES AUXILIARES ---

    // 1. Mostrar el menú de "¿Qué deseas agregar?"
    private fun mostrarDialogoSeleccion() {
        val opciones = arrayOf("Nueva Materia", "Nueva Tarea")

        AlertDialog.Builder(this)
            .setTitle("¿Qué deseas agregar?")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> { // Nueva Materia
                        startActivity(Intent(this, AddSubjectActivity::class.java))
                    }
                    1 -> { // Nueva Tarea
                        validarYRedireccionarTarea()
                    }
                }
            }
            .show()
    }

    // 2. Validar si hay materias antes de crear tarea
    private fun validarYRedireccionarTarea() {
        if (currentMaterias.isEmpty()) {
            // Mostrar Alerta si no hay materias
            AlertDialog.Builder(this)
                .setTitle("¡Atención!")
                .setMessage("Para crear una tarea, primero necesitas registrar al menos una materia.")
                .setPositiveButton("Entendido", null)
                .show()
        } else {
            // Si hay materias, vamos al formulario
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    // 3. Alertas de borrado (copiadas para tener el código limpio en MainActivity)
    private fun mostrarConfirmacionBorrarMateria(materia: Materia) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Materia")
            .setMessage("¿Eliminar ${materia.nombreMateria} y todas sus tareas?")
            .setPositiveButton("Sí") { _, _ ->
                materiaViewModel.eliminarMateria(materia)
                Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun mostrarConfirmacionBorrarTarea(nombre: String, id: Int, fecha: String, completada: Boolean, materiaId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Tarea")
            .setMessage("¿Eliminar '$nombre'?")
            .setPositiveButton("Sí") { _, _ ->
                val t = Tarea(id, nombre, fecha, completada, materiaId)
                tareaViewModel.eliminarTarea(t)
                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
}