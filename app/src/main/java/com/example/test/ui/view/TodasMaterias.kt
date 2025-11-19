package com.example.test.ui.view


import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.model.Tarea
import com.example.test.data.repository.MateriaRepository
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory



class TodasMaterias : AppCompatActivity() {

    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.todas_materias)


        // RecyclerView de materias
        val recyclerMaterias = findViewById<RecyclerView>(R.id.recyclerMaterias)
        val materiaAdapter = MateriaAdapter(emptyList())
        recyclerMaterias.adapter = materiaAdapter
        recyclerMaterias.layoutManager = LinearLayoutManager(this)

        val search = findViewById<EditText>(R.id.barra_busqueda)
        val lupa = findViewById<ImageView>(R.id.boton_buscar)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // db
        val db = DatabaseProvider.getDatabase(this)









        // --- MATERIA ---


        // dao
        val materiaDao = db.materiaDao()

        // repo
        val materiaRepository = MateriaRepository(materiaDao)

        // Factory y ViewModel
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)






        materiaViewModel.materias.observe(this) { lista ->
            materiaAdapter.updateList(lista)
        }



        lupa.setOnClickListener {
            val textoBusqueda = search.text.toString().trim()
            if (textoBusqueda.isNotEmpty()) {
                materiaViewModel.buscarMaterias(textoBusqueda).observe(this) { lista ->
                    materiaAdapter.updateList(lista)
                }
            } else {
                // No vuelvas a observar, solo restablece la lista actual
                materiaViewModel.materias.value?.let { lista ->
                    materiaAdapter.updateList(lista)
                }
            }
        }






        }


    }
