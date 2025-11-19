package com.example.test.ui.view


import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import com.example.test.data.database.DatabaseProvider
import com.example.test.data.model.Tarea
import com.example.test.data.repository.MateriaRepository
import com.example.test.data.repository.TareaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory
import com.example.test.ui.viewModel.TareaViewModel
import com.example.test.ui.viewModel.TareaViewModelFactory



class AddTaskActivity : AppCompatActivity() {

    private lateinit var tareaViewModel: TareaViewModel
    private lateinit var materiaViewModel: MateriaViewModel

    override fun onCreate(savedInstanceState: Bundle?){

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)

        val titulo = findViewById<EditText>(R.id.titulo)
        val descripcion = findViewById<EditText>(R.id.descripcion)
        val fecha = findViewById<DatePicker>(R.id.fecha)
        val spinnerMateria = findViewById<Spinner>(R.id.materia)
        val enviar = findViewById<Button>(R.id.enviar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // db
        val db = DatabaseProvider.getDatabase(this)


        // --- TAREA ---


        // dao
        val tareaDao = db.tareaDao()

        // repo
        val tareaRepository = TareaRepository(tareaDao)

        // Factory y ViewModel
        val tareaFactory = TareaViewModelFactory(tareaRepository)
        tareaViewModel = ViewModelProvider(this, tareaFactory).get(TareaViewModel::class.java)

        // Observador para verificar en tiempo real si hubo un error
        tareaViewModel.insercionExitosa.observe(this) { exito ->
            if(exito){
                Toast.makeText(this, "Tarea insertada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cerramos la pestana en la que tenemos el formulario agregar tarea
            }
            else{
                Toast.makeText(this, "Error al insertar la tarea", Toast.LENGTH_SHORT).show()

                // Limpiar datos
                titulo.text.clear()
                descripcion.text.clear()
                spinnerMateria.setSelection(0)
                val hoy = java.util.Calendar.getInstance()
                fecha.updateDate(hoy.get(java.util.Calendar.YEAR), hoy.get(java.util.Calendar.MONTH), hoy.get(java.util.Calendar.DAY_OF_MONTH))
            }
        }



        // Observador
        tareaViewModel.tareas.observe(this, Observer { lista ->
            println("Tareas actuales: $lista")
        })



        // --- MATERIA ---


        // dao
        val materiaDao = db.materiaDao()

        // repo
        val materiaRepository = MateriaRepository(materiaDao)

        // Factory y ViewModel
        val materiaFactory = MateriaViewModelFactory(materiaRepository)
        materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)




        materiaViewModel.materias.observe(this) { lista ->
            // Llenar spinner con materias
            val nombres = lista.map { it.nombreMateria }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMateria.adapter = adapter
        }



        // -- Boton para crear tarea --
        enviar.setOnClickListener {

            val tituloTarea = titulo.text.toString()
            val desc = descripcion.text.toString()
            val dia = fecha.dayOfMonth
            val mes = fecha.month + 1
            val año = fecha.year
            val fechaEntrega = "$año-$mes-$dia"

            val materiaSeleccionada = spinnerMateria.selectedItemPosition
            val listaMaterias = materiaViewModel.materias.value
            if(listaMaterias.isNullOrEmpty() || materiaSeleccionada < 0){
                Toast.makeText(this, "Selecciona una materia valida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val materiaId = listaMaterias[materiaSeleccionada].id

            val nuevaTarea = Tarea(
                id = 0,
                nombreTarea = tituloTarea,
                fechaEntrega = fechaEntrega,
                completada = false,
                materiaId = materiaId
            )

            tareaViewModel.agregarTarea(nuevaTarea)


        }


    }
}