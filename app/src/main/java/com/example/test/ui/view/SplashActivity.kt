package com.example.test.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Ocultar la barra de acción (ActionBar) para que se vea pantalla completa limpia
        supportActionBar?.hide()

        // Usamos una corrutina para esperar 2 segundos
        lifecycleScope.launch {
            delay(2000) // 2000 milisegundos = 2 segundos

            // Después de 2 segundos, abrimos la MainActivity
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)

            // Cerramos esta actividad para que si le dan "Atrás" no vuelvan al logo
            finish()
        }
    }
}