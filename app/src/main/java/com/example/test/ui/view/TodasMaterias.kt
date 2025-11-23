package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog // Importante para el diálogo de confirmación
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.repository.MateriaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TodasMaterias : AppCompatActivity() {

    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todas_materias)

        // Referencias UI
        val recyclerMaterias = findViewById<RecyclerView>(R.id.recyclerMaterias)
        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)
        val btnRegresar = findViewById<FloatingActionButton>(R.id.buttonRegresar)
        val btnAgregarTop = findViewById<FloatingActionButton>(R.id.fabAgregarMateriaTop)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. ViewModel
        val db = DatabaseProvider.getDatabase(this)
        val materiaDao = db.materiaDao()
        val materiaRepository = MateriaRepository(materiaDao)
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)

        // 2. Configurar Adapter (AHORA CON LOS 3 PARAMETROS REQUERIDOS)
        val adapter = MateriaAdapter(
            materias = emptyList(),
            onItemClick = { materiaClicked ->
                // Clic Normal: Ir al detalle y actualizar fecha de uso
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
                // Clic en Eliminar (Desde los 3 puntos): Mostrar Alerta
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Materia")
                    .setMessage("¿Estás seguro de eliminar ${materia.nombreMateria}? Se borrarán todas sus tareas.")
                    .setPositiveButton("Sí") { _, _ ->
                        materiaViewModel.eliminarMateria(materia)
                        Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        recyclerMaterias.adapter = adapter
        recyclerMaterias.layoutManager = LinearLayoutManager(this) // Lista vertical

        // 3. Observadores
        materiaViewModel.materias.observe(this) { lista ->
            adapter.updateList(lista)
        }

        // 4. Botones
        btnRegresar.setOnClickListener { finish() }
        btnAgregarTop.setOnClickListener { startActivity(Intent(this, AddSubjectActivity::class.java)) }

        // 5. Buscador
        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                materiaViewModel.buscarMaterias(textoBusqueda).observe(this) { lista ->
                    adapter.updateList(lista)
                }
            } else {
                materiaViewModel.materias.value?.let { lista ->
                    adapter.updateList(lista)
                }
            }
        }
    }
}