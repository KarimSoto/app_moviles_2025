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
import com.example.test.data.model.Materia
import com.example.test.data.repository.MateriaRepository
import com.example.test.ui.viewModel.MateriaViewModel
import com.example.test.ui.viewModel.MateriaViewModelFactory





class AddSubjectActivity : AppCompatActivity() {

    private lateinit var materiaViewModel: MateriaViewModel

        override fun onCreate(savedInstanceState: Bundle?){

            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_add_subject)

            val titulo = findViewById<EditText>(R.id.materia)
            val enviar = findViewById<Button>(R.id.enviar)


            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }


            // db
            val db = DatabaseProvider.getDatabase(this)



            // MATERIAS

            // dao
            val materiaDao = db.materiaDao()

            // repo
            val materiaRepository = MateriaRepository(materiaDao)

            // Factory y ViewModel
            val materiaFactory = MateriaViewModelFactory(materiaRepository)
            materiaViewModel = ViewModelProvider(this, materiaFactory).get(MateriaViewModel::class.java)


            materiaViewModel.insercionExitosa.observe(this) { exito->
                if(exito){
                    Toast.makeText(this, "Materia insertada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else{
                    Toast.makeText(this, "Error al insertar la materia", Toast.LENGTH_SHORT).show()
                    titulo.text.clear()
                }
            }


            enviar.setOnClickListener{

                val tituloMateria = titulo.text.toString()

                val nuevaMateria = Materia(
                    id = 0,
                    nombreMateria = tituloMateria
                )

                materiaViewModel.agregarMateria(nuevaMateria)

            }

        }
}