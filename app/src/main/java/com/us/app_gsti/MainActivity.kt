package com.us.app_gsti

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.us.app_gsti.pantallas.paciente.MapasPacienteActivity

import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {

    ////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hideSystemBars()

        val tarea = object : TimerTask() {
            override fun run() {
                intent = Intent(this@MainActivity,  AuthActivity::class.java)

                val options = ActivityOptions.makeCustomAnimation(
                    this@MainActivity, R.transition.fade_in, R.transition.fade_out
                )

                startActivity(intent, options.toBundle())
                finish()
            }
        }

        val tiempo = Timer()
        tiempo.schedule(tarea, 1800L) // Cambia el valor 500L a la cantidad de milisegundos que deseas para el retraso


    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }



} //LLAVE FINAL