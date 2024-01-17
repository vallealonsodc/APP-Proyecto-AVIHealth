package com.us.app_gsti.pantallas.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import java.text.SimpleDateFormat

/*sugerencias de cambios en estética:
1. Boton se llame registro
2 . El activity y el xml se llame RegistrarPacienteActivity
*/
class CrearPacienteActivity : AppCompatActivity() {

    //DEFINICION DE VARIABLES que usaremos
    private lateinit var email: EditText
    private lateinit var passwd: EditText
    private lateinit var btnRegistro: Button

    private lateinit var nombre: EditText
    private lateinit var apellidos: EditText
    private lateinit var nacimiento: EditText
    private lateinit var telefono: EditText
    private lateinit var btn_volverAtras: Button


    //creremos la var que nos servira para comuniacrnos con firebase
    private lateinit var auth: FirebaseAuth

    ///////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_paciente)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


        //inicializacion de los valores de las variables
        btnRegistro = findViewById(R.id.btnCrearPaciente)
        email = findViewById(R.id.edit_txt_email)
        passwd= findViewById(R.id.edit_txt_contraseña)
        auth= Firebase.auth
        btn_volverAtras = findViewById(R.id.btn_volverAtras)
        nombre= findViewById(R.id.edit_txt_nombre)
        apellidos= findViewById(R.id.edit_txt_apellidos)
        nacimiento= findViewById(R.id.edit_txt_fecha_nacimiento)
        telefono= findViewById(R.id.edit_txt_telefono)

        val resValidacion= false

        // llama a la funcion implemeta toda la lógica del boton registro
        logica_btnCrear()


        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }


    } //LLAVE ON CRETAE


    //IMPORTANTE:
    //para que no se cree el usuario y despues de fallo la validación de datos, he incluido la nueva condicion en el if
    fun logica_btnCrear() {
        // Aquí pondremos la lógica de los botones de autenticación
        //lógica de boton registrarse
        btnRegistro.setOnClickListener {
            val passwdPordefecto= false
            //si no pone ninguna contraseña, por defecto, pone lo que este delante del @ si cumple con las restricciones
            if (passwd.text.isBlank()){
                val passwdPordefecto= true
                val contraseñaPorDefecto = email.text.toString().substringBefore('@')
                val regex = Regex("^[A-Z](?=.*\\d).{7,}\$")
                if (regex.matches(contraseñaPorDefecto)) {
                    passwd.setText(contraseñaPorDefecto)
                }
            }

            // Comprobar que el correo electrónico y la contraseña no estén vacíos
            //revisa que tambien el formato de los datos de entrada
            if (email.text.isNotBlank() && passwd.text.isNotBlank() && validacionDatos()) {
                // sí podemos registrar al usuario
                auth.createUserWithEmailAndPassword(
                    email.text.toString(),
                    passwd.text.toString()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) { //si se ha hecho de la forma correcta -> TASK= a la función que espera para ejecutarse
                        // El registro se ha hecho de forma correcta
                        Log.i("INFO", "El usuario se ha registrado correctamente")
                        //cuando se registra, se actualiza la pantalla y limpia los valores que se habían puesto en esos campos
                        almacenarInfoBD(passwdPordefecto)
                        email.text.clear()
                        passwd.text.clear()
                        nombre.text.clear()
                        nombre.text.clear()
                        apellidos.text.clear()
                        nacimiento.text.clear()
                        telefono.text.clear()
                        //si no se ha realizado de forma correcta
                        //crea al paciente en la bd
                        intent = Intent(this, CitasMedicoActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Si ha habido algún fallo que aparezca un Toast
                        //Toast.makeText(this,"Fallo en el registro", Toast.LENGTH_SHORT).show()
                        //si ha habido un fallo que apararezca un alertdialog
                        showAlert("Ha habido un fallo en el registro")
                    }
                }
            }else{
                showAlert("Para no poner la contraseña, el correo debe de cumplir con el formato adecuado")
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
            .setMessage(text)
            .setTitle("Mensaje")
            .setPositiveButton("ACEPTAR", null)
        //crear el dialog
        val dialog: AlertDialog = builder.create()
        //mostrar el dialog
        dialog.show()
    }
    //////////////////////////////////////7

    /////////////////////////////////////////////
    //VALIDACIÓN de datos paciente
    //datos que son compatibles con la validacion: +34612345678 y  01/01/2023

    private fun validacionDatos(): Boolean {
        //REGEX CREA UNA VALIDACIÓN -> el n telefono debe de empezar por 34, 0034 o 34 , después debe de haber 67  y debe de tener 8 digitos
        //se crea la var e1 que contiene una validación
        //Comprobar expresiín del numero telefono

        val telefonoRegex = Regex("^(\\+34|0034|34)?[67]\\d{8}\$")
        val ok1= telefonoRegex.matches(telefono.text.toString())
        //aplica la validación al campo text de la variable telefono (conectada a a la view)
        if(!telefonoRegex.matches(telefono.text.toString())){
            // Error
            Log.e("ERROR", "El télefono introducido no es correcto")
            telefono.error = "Teléfono incorrecto"
        }

        //Comprobar expresión regular de fecha: dd/MM/aaaa

        val fechaRegex = Regex("^([0-2][0-9]|3[0-1])(\\/|-)(0[1-9]|1[0-2])\\2(\\d{4})\$")
        val ok2= fechaRegex.matches(nacimiento.text.toString())
        if(!fechaRegex.matches(nacimiento.text.toString())){
            // Error
            Log.e("ERROR", "La fecha introducido no es correcto")
            nacimiento.error = "Fecha incorrecto"
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        val ok3= emailRegex.matches(email.text.toString())
        if(!emailRegex.matches(email.text.toString())){
            // Error
            Log.e("ERROR", "El formato de email introducido no es correcto")
            email.error = "Email incorrecto"
        }

        val passwdRegex = Regex("^[A-Z](?=.*\\d).{7,}\$")
        val ok4= passwdRegex.matches(passwd.text.toString())
        if(!passwdRegex.matches(passwd.text.toString())){
            // Error
            Log.e("ERROR", "El formato de contraseña introducido no es correcto")
            passwd.error = "Contraseña incorrecta"
        }

        // Validar que el campo de nombre no esté vacío
        val ok5= nombre.text.isBlank()
        if (ok5) {
            // Error
            Log.e("AVISO", "El campo de nombre no puede estar vacío")
            nombre.error = "Nombre requerido"
        }

        // Validar que el campo de apellidos no esté vacío
        val ok6= apellidos.text.isBlank()
        if (ok6) {
            // Error
            Log.e("AVISO", "El campo de apellidos no puede estar vacío")
            apellidos.error = "Apellidos requeridos"
        }

        return ok1 && ok2 && ok3 && ok4 && !ok5 && !ok6


    }

    ///////////////////////////////////

    ///////////////////////////////////
    private fun almacenarInfoBD(passwdPordefecto: Boolean){
        //convertir de string a timestamp para almacenarla
        //crear una instancia de tipo formato
        var formato = SimpleDateFormat("dd/MM/yyyy")
        // con ese tipo formato convertir a tipo date
        var fecha = formato.parse(nacimiento.text.toString())
        //convertir objeto date a objeto timestamp de firebase
        //esta es la variable que hay que almacenar -> usar el tipo de Firebase
        var timestamp= Timestamp(fecha)


        //definir un documento a mano en el sitio web de firebase -> cloud firestore
        //el substring es para que en la bd no se guarde los espacios de Sexo: Hombre. (el 6 es un código predefinido)
        var nuevaInfo = mapOf(
            "nombre" to nombre.text.toString(),
            "apellidos" to apellidos.text.toString(),
            "telefono" to telefono.text.toString(),
            "fecha de nacimiento" to timestamp
        )

        //colección: "paciente"
        //id del documento -> email del paciente autentificado
        //acceder a la bd -> usarlo siempre para introducir o extraer datos
        val myBD = FirebaseFirestore.getInstance()
        //si no esta creada la coleccion la crea y si no, accede
        val myCol = myBD.collection("pacientes")
        //guardar en la bd, en la colección pacientes, un documento
        //email es el id, en el set se le pasa el documento
        myCol.document(email.text.toString()).set(nuevaInfo).addOnSuccessListener {
            //showalert
            if (passwdPordefecto){showAlert("Usuario registrado correctamente con contraseña por defecto")}else{showAlert("Usuario registrado correctamente")}

        }.addOnFailureListener{
            //showalert
            showAlert("Error en el registro del usuario")
        }

    } //FIN almacenarInfoBD()


    ///////////////////////////////////



} // LLAVE FINAL