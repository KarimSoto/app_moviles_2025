package com.example.test.ui.view

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
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

class TodasTareas : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todas_tareas)

        // --- RECYCLER ---
        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareas)
        val tareaAdapter = TareaAdapter(emptyList())
        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)

        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- BASE DE DATOS ---
        val db = DatabaseProvider.getDatabase(this)

        // --- TAREA ---
        val tareaDao = db.tareaDao()
        val tareaRepository = TareaRepository(tareaDao)
        val tareaFactory = TareaViewModelFactory(tareaRepository)
        tareaViewModel = ViewModelProvider(this, tareaFactory).get(TareaViewModel::class.java)

        // --- MATERIA ---
        val materiaDao = db.materiaDao()
        val materiaRepository = MateriaRepository(materiaDao)
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)

        // --- OBSERVAR TAREAS + MATERIAS ---
        tareaViewModel.tareas.observe(this) { listaTareas ->
            materiaViewModel.materias.observe(this) { listaMaterias ->
                val tareasConMateria = combinarTareasConMaterias(listaTareas, listaMaterias)
                tareaAdapter.updateList(tareasConMateria)
            }
        }

        // --- BUSCADOR ---
        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                tareaViewModel.buscarTareas(textoBusqueda).observe(this) { listaTareas ->
                    materiaViewModel.materias.observe(this) { listaMaterias ->
                        val tareasConMateria = combinarTareasConMaterias(listaTareas, listaMaterias)
                        tareaAdapter.updateList(tareasConMateria)
                    }
                }
            } else {
                tareaViewModel.tareas.value?.let { listaTareas ->
                    materiaViewModel.materias.value?.let { listaMaterias ->
                        val tareasConMateria = combinarTareasConMaterias(listaTareas, listaMaterias)
                        tareaAdapter.updateList(tareasConMateria)
                    }
                }
            }
        }
    }

    // 🔹 Función auxiliar para combinar Tareas con Materias
    private fun combinarTareasConMaterias(
        tareas: List<Tarea>,
        materias: List<Materia>
    ): List<TareaConMateria> {
        return tareas.map { tarea ->
            val materiaNombre = materias.find { it.id == tarea.materiaId }?.nombreMateria ?: "Sin materia"
            TareaConMateria(
                id = tarea.id,
                nombreTarea = tarea.nombreTarea,
                fechaEntrega = tarea.fechaEntrega,
                completada = tarea.completada,
                materiaId = tarea.materiaId,
                materiaNombre = materiaNombre
            )
        }
    }
}
