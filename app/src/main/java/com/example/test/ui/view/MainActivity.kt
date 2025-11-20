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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- DB y ViewModels (Inicializamos esto antes para usarlo en el adaptador) ---
        val db = DatabaseProvider.getDatabase(this)

        val tareaDao = db.tareaDao()
        val tareaRepository = TareaRepository(tareaDao)
        val tareaFactory = TareaViewModelFactory(tareaRepository)
        tareaViewModel = ViewModelProvider(this, tareaFactory).get(TareaViewModel::class.java)

        val materiaDao = db.materiaDao()
        val materiaRepository = MateriaRepository(materiaDao)
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)


        // --- Configuración de RecyclerViews ---
        val recyclerMaterias = findViewById<RecyclerView>(R.id.recyclerMaterias)

        // CAMBIO PRINCIPAL AQUÍ: Pasamos la función lambda al adaptador
        materiaAdapter = MateriaAdapter(emptyList()) { materiaClicked ->
            // 1. Avisamos al ViewModel que se usó esta materia (para actualizar la fecha)
            materiaViewModel.updateMateriaInteraction(materiaClicked)

            // 2. Navegamos a la pantalla de detalle
            val intent = Intent(this, DetalleMateriaActivity::class.java)
            intent.putExtra("nombreMateria", materiaClicked.nombreMateria)
            intent.putExtra("id", materiaClicked.id)
            startActivity(intent)
        }

        recyclerMaterias.adapter = materiaAdapter

        // Layout Manager de Cuadrícula (2 columnas)
        recyclerMaterias.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)

        // Desactivar scroll anidado
        recyclerMaterias.isNestedScrollingEnabled = false


        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareas)
        tareaAdapter = TareaAdapter(emptyList())
        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)

        // --- Elementos UI ---
        val search = findViewById<EditText>(R.id.barra_busqueda)
        val addSubject = findViewById<FloatingActionButton>(R.id.redireccionarMateria)
        val addTask = findViewById<Button>(R.id.redireccionarTarea)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)
        val todasMaterias = findViewById<TextView>(R.id.buttonVerTodo)
        val todasTareas = findViewById<TextView>(R.id.buttonVerTodasTareas)
        val home = findViewById<Button>(R.id.home)
        val botonCompletados = findViewById<Button>(R.id.ButtonCompletados)
        val botonPendientes = findViewById<Button>(R.id.ButtonPendientes)


        // --- Observadores ---
        // Observamos 'recentMaterias' para que salgan las 4 últimas/más usadas
        materiaViewModel.recentMaterias.observe(this) { lista ->
            materiaAdapter.updateList(lista)
        }

        tareaViewModel.tareas.observe(this) { listaTareas ->
            materiaViewModel.materias.observe(this) { listaMaterias ->
                val tareasConMateria = listaTareas.map { tarea ->
                    val materiaNombre = listaMaterias.find { it.id == tarea.materiaId }?.nombreMateria ?: "Sin materia"
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
        }

        // --- Función para combinar tareas + materias ---
        fun combinarTareasConMateria(listaTareas: List<com.example.test.data.model.Tarea>) {
            materiaViewModel.materias.observe(this) { listaMaterias ->
                val tareasConMateria = listaTareas.map { tarea ->
                    val materiaNombre = listaMaterias.find { it.id == tarea.materiaId }?.nombreMateria ?: "Sin materia"
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
        }

        // --- Botón lupa (buscar) ---
        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                materiaViewModel.buscarMaterias(textoBusqueda).observe(this) { lista ->
                    materiaAdapter.updateList(lista)
                }

                tareaViewModel.buscarTareas(textoBusqueda).observe(this) { lista ->
                    combinarTareasConMateria(lista)
                }
            } else {
                // Al borrar búsqueda, volver a mostrar las recientes
                materiaViewModel.recentMaterias.observe(this) { lista ->
                    materiaAdapter.updateList(lista)
                }
                tareaViewModel.tareas.observe(this) { lista ->
                    combinarTareasConMateria(lista)
                }
            }
        }

        // --- Botón home ---
        home.setOnClickListener {
            search.text.clear()
            search.clearFocus()

            // Home muestra las recientes
            materiaViewModel.recentMaterias.observe(this) { lista ->
                materiaAdapter.updateList(lista)
            }

            tareaViewModel.tareas.observe(this) { lista ->
                combinarTareasConMateria(lista)
            }
        }

        // --- Botones para navegar ---
        todasMaterias.setOnClickListener {
            startActivity(Intent(this, TodasMaterias::class.java))
        }

        todasTareas.setOnClickListener {
            startActivity(Intent(this, TodasTareas::class.java))
        }

        addTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        addSubject.setOnClickListener {
            startActivity(Intent(this, AddSubjectActivity::class.java))
        }

        // --- Botones de filtrado ---
        botonCompletados.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(true).observe(this) { lista ->
                combinarTareasConMateria(lista)
            }
        }

        botonPendientes.setOnClickListener {
            tareaViewModel.buscarTareasPorEstado(false).observe(this) { lista ->
                combinarTareasConMateria(lista)
            }
        }

        // --- Ajuste de bordes ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}