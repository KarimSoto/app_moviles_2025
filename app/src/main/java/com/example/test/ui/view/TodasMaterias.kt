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

        // 2. Configurar el NUEVO ADAPTADOR (MateriaListaAdapter)
        val adapter = MateriaListaAdapter(emptyList()) { materiaClicked ->
            // Lógica al hacer clic en un elemento de la lista
            materiaViewModel.updateMateriaInteraction(materiaClicked)

            val intent = Intent(this, DetalleMateriaActivity::class.java)
            intent.putExtra("nombreMateria", materiaClicked.nombreMateria)
            intent.putExtra("id", materiaClicked.id)
            startActivity(intent)
        }

        recyclerMaterias.adapter = adapter
        recyclerMaterias.layoutManager = LinearLayoutManager(this) // Lista vertical

        // 3. Observadores
        // Aquí usamos getAllMaterias (o el liveData general) para ver TODAS
        materiaViewModel.materias.observe(this) { lista ->
            adapter.updateList(lista)
        }

        // 4. Botones

        // Botón de Regresar (Abajo)
        btnRegresar.setOnClickListener {
            finish()
        }

        // Botón de Agregar Materia (Arriba a la derecha)
        btnAgregarTop.setOnClickListener {
            startActivity(Intent(this, AddSubjectActivity::class.java))
        }

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