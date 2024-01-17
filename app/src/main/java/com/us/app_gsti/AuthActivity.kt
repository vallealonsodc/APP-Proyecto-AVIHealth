package com.us.app_gsti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod

import com.us.app_gsti.pantallas.paciente.CitasPacienteActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.auth
import com.us.app_gsti.pantallas.medico.CitasMedicoActivity
import com.us.app_gsti.pantallas.medico.CrearTeleconsultaActivity


class AuthActivity : AppCompatActivity() {


    //definimos las variables
    private lateinit var email: EditText
    private lateinit var passwd: EditText
    private lateinit var btnAcceder: Button
    //creremos la var que nos servira para comuniacrnos con firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var togglePasswordVisibility: ImageView

    ////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        btnAcceder= findViewById(R.id.btnLogin)
        email = findViewById(R.id.editTextEmail)
        passwd= findViewById(R.id.editTextPassword)
        auth= Firebase.auth


        setup()

        togglePasswordVisibility = findViewById(R.id.imageViewTogglePassword)

        setupPasswordToggle()

    } //LLAVE ONCREATE


    /////////////////////////////////////
    fun setup(){
        //lógica de botón de acceder
        btnAcceder.setOnClickListener {
            // Comprobar que el correo electrónico y la contraseña no estén vacíos
            if(email.text.isNotBlank() && passwd.text.isNotBlank()){
                // sí podemos acceder en la app
                auth.signInWithEmailAndPassword(
                    email.text.toString(),
                    passwd.text.toString()
                ).addOnCompleteListener{task ->
                    //IMPORTANTE --> espera a que la funcion de la cual se llama termine
                    if (task.isSuccessful){ //si se ha hecho de la forma correcta -> TASK= a la función que espera para ejecutarse
                        // El acceso se ha hecho de forma correcta
                        Log.i("INFO", "El usuario se ha logueado correctamente")
                        //si los datos son correctos, se redirecciona a su activity pertinente
                        showHomeAuth(email.text.toString())
                        //cuando se registra, se actualiza la pantalla y limpia los valores que se habían puesto en esos campos
                        email.text.clear()
                        passwd.text.clear()
                        //si no se ha realizado de forma correcta
                    } else{
                        // Si ha habido algún fallo que aparezca un Toast
                        //Toast.makeText(this,"Fallo en el registro", Toast.LENGTH_SHORT).show()
                        //si ha habido un fallo que apararezca un alertdialog -> si se intenta acceder con un usuario que existe, pero fallla en la contraseña
                        showAlert("Ha habido un fallo en el acceso")
                    }
                }
            }

        }
    }

    /////////////////////////////////////////////////////////////////

    //función que recibe una cadena de texo que mostrará como dialogo de alerta --> se usa en ambos botones
    private fun showAlert(text: String){
        //definimos el builder -> es un patrón de diseño de ing software, sirve para construir objetos muy complejos de forma muy sencilla
        //básicamente funciona como constructor
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        //añadimos propiedades al builder
        builder
            .setMessage("Error")
            .setTitle(text)
            .setPositiveButton("ACEPTAR", null)
        //crear el dialog
        val dialog: AlertDialog = builder.create()
        //mostrar el dialog
        dialog.show()
    }
    //////////////////////////////////////7////



    //////////////////////////////////////7

    //función que decide al loguease un usuario, si el usuario se va al activity de medico o de paciente
    private fun showHomeAuth(email: String) {
        //Acceder a la coleccion de pacientes y mirar si está
        val myCol = FirebaseFirestore.getInstance().collection("pacientes")
        myCol.document(email).get().addOnSuccessListener {
            if (it.exists()) { //¿Es paciente? -> si está es que es paciente
                startActivity(Intent(this, CitasPacienteActivity::class.java))
            } else {
                startActivity(Intent(this, CitasMedicoActivity::class.java))
            }
        }
    }
    /////////////////////////////////

    private fun setupPasswordToggle() {
        var passwordVisible = false
        togglePasswordVisibility.setOnClickListener {
            passwordVisible = !passwordVisible
            passwd.transformationMethod = if (passwordVisible) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            passwd.setSelection(passwd.text.length) // Move cursor to the end of the text
            togglePasswordVisibility.setImageResource(
                if (passwordVisible) R.drawable.ic_ojo // Replace with your eye-off icon resource
                else R.drawable.ic_ojo_cruzado // Replace with your eye icon resource
            )
        }
    }

} //LLAVE FINAL