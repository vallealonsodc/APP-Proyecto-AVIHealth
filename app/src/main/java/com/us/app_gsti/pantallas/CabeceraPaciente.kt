package com.us.app_gsti.pantallas

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.us.app_gsti.AuthActivity
import com.us.app_gsti.MainActivity
import com.us.app_gsti.R

open class CabeceraPaciente : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    fun showMenuPaciente(v: View) {
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_paciente, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.menu_mi_perfil -> {
                    startActivity(Intent(this, InfoActivity::class.java))
                    // Acción para "Mi Perfil"

                    true
                }
                R.id.menu_cerrar_sesion -> {
                    // Acción para "Cerrar Sesión"
                    Firebase.auth.signOut()
                    startActivity(Intent(this, AuthActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}