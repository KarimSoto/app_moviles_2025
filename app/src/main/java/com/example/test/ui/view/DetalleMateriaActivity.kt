package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.example.test.data.model.Tarea
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DetalleMateriaActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var tareaAdapter: TareaAdapter

    // Lista completa para filtrar en el buscador sin ir a la BD cada vez
    private var listaCompletaTareas: List<Tarea> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_materia)

        // Ajuste de bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Obtener datos del Intent
        val materiaId = intent.getIntExtra("id", -1)
        val nombreMateriaStr = intent.getStringExtra("nombreMateria") ?: "Detalle de Materia"

        // 2. Referencias UI
        val tituloMateria = findViewById<TextView>(R.id.tituloMateriaDetalle)
        val recyclerTareas = findViewById<RecyclerView>(R.id.recyclerTareasMateria)
        val textSinTareas = findViewById<TextView>(R.id.textSinTareas)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresarDetalle)
        val btnAgregarTarea = findViewById<FloatingActionButton>(R.id.fabAgregarTareaMateria)
        val inputBusqueda = findViewById<EditText>(R.id.barra_busqueda)
        val botonBuscar = findViewById<ImageView>(R.id.boton_buscar)

        tituloMateria.text = nombreMateriaStr

        // 3. Configurar ViewModel
        val db = DatabaseProvider.getDatabase(this)
        val tareaRepo = TareaRepository(db.tareaDao())
        tareaViewModel = ViewModelProvider(this, TareaViewModelFactory(tareaRepo)).get(TareaViewModel::class.java)

        // 4. Configurar Adaptador
        tareaAdapter = TareaAdapter(emptyList())
        recyclerTareas.adapter = tareaAdapter
        recyclerTareas.layoutManager = LinearLayoutManager(this)

        // Función auxiliar para actualizar la lista en pantalla
        fun actualizarLista(lista: List<Tarea>) {
            if (lista.isNullOrEmpty()) {
                recyclerTareas.visibility = View.GONE
                textSinTareas.visibility = View.VISIBLE
            } else {
                recyclerTareas.visibility = View.VISIBLE
                textSinTareas.visibility = View.GONE

                val tareasConMateria = lista.map { tarea ->
                    TareaConMateria(
                        id = tarea.id,
                        nombreTarea = tarea.nombreTarea,
                        fechaEntrega = tarea.fechaEntrega,
                        completada = tarea.completada,
                        materiaId = tarea.materiaId,
                        materiaNombre = nombreMateriaStr
                    )
                }
                tareaAdapter.updateList(tareasConMateria)
            }
        }

        // 5. Observar tareas de esta materia
        if (materiaId != -1) {
            tareaViewModel.getTareasByMateriaId(materiaId).observe(this) { listaTareas ->
                listaCompletaTareas = listaTareas // Guardamos la copia original
                actualizarLista(listaTareas)      // Mostramos todo al inicio
            }
        }

        // 6. Lógica del Buscador
        botonBuscar.setOnClickListener {
            val query = inputBusqueda.text.toString().trim().lowercase()
            if (query.isNotEmpty()) {
                // Filtramos la lista que ya tenemos en memoria
                val listaFiltrada = listaCompletaTareas.filter {
                    it.nombreTarea.lowercase().contains(query)
                }
                actualizarLista(listaFiltrada)
            } else {
                // Si está vacío, restauramos la lista original
                actualizarLista(listaCompletaTareas)
            }
        }

        // 7. Botón Agregar Tarea (+)
        btnAgregarTarea.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            // Podríamos pasar el materiaId aquí si quisieras pre-seleccionar la materia en el futuro
            startActivity(intent)
        }

        // 8. Botón Regresar
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}