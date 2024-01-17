package com.us.app_gsti.pantallas

import android.content.ContentValues
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
import com.us.app_gsti.MainActivity
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.paciente.CitasPacienteActivity
import java.text.SimpleDateFormat



class InfoActivity : AppCompatActivity() {

    private lateinit var textNombre: EditText
    private lateinit var textContraseña: EditText
    private lateinit var textEmail: EditText
    private lateinit var telefono: EditText
    private lateinit var textFechaNacimiento: EditText
    private lateinit var textApellido: EditText
    private lateinit var btnActualizar: Button
    private lateinit var btnVolverAtras: Button
    private lateinit var btnEliminarCuenta: Button
    //creremos la var que nos servira para comuniacrnos con firebase
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


        //inicializa las variables

        textNombre=findViewById(R.id.nombreEditText)
        telefono = findViewById(R.id.phoneEditText)
        textEmail= findViewById(R.id.emailEditText)
        textContraseña= findViewById(R.id.contraseñaEditText)
        textFechaNacimiento= findViewById(R.id.fechaEditText)
        textApellido= findViewById(R.id.apellidosEditText)
        btnActualizar= findViewById(R.id.btnActualizarDatos)
        btnVolverAtras = findViewById(R.id.btnVolverAtras)
        btnEliminarCuenta= findViewById(R.id.btnEliminarCuenta)
        auth= Firebase.auth

        recuperar_userAutenticado()
        mostrarInfo()

        logica_actualizar()
        logica_borrar()

        btnVolverAtras.setOnClickListener {
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }


    }

    ////////////////////////////////////////////
    private fun logica_actualizar(){
        btnActualizar.setOnClickListener {

            if (validacionDatos()) {
                //showAlert("Hasta aqui funciona")
                almacenarInfoBD()
            }
        }
    }
    /////////////////////////

    /*
    El problema aquí está relacionado con la naturaleza asíncrona de la llamada a la base de datos. La función addOnSuccessListener se ejecuta de manera asíncrona, y mientras la respuesta de la base de datos no haya llegado, la función reconocerRol ya habrá devuelto el valor predeterminado "pacientes".

    Para manejar llamadas asíncronas, puedes usar callbacks o listeners. Una forma de hacer esto es modificar la función reconocerRol para que acepte un callback y llame a ese callback cuando se obtenga la respuesta de la base de datos
     */
    private fun reconocerRol(callback: (String) -> Unit) {
            var resultado = "pacientes"
            //Acceder a la coleccion de pacientes y mirar si está
            val emailToSearch = textEmail.text.toString()
            // Poner en mayusucla solo la primera letra a mayúsculas
            val emailPrimeraMayusucla = emailToSearch.substring(0, 1).toUpperCase() + emailToSearch.substring(1)
            val myCol = FirebaseFirestore.getInstance().collection("pacientes")
        myCol.document(emailPrimeraMayusucla).get().addOnSuccessListener {
            if (it.exists()) { //¿Es paciente? -> si está es que es paciente
                callback("pacientes")
            } else {
                callback("medicos")
            }
        }
    }
    //////////////////////////////
    //recuperar datos de la bd y mostrarlos en la etiquets de formulario
    private fun mostrarInfo(){

        // Llamada a la función con un callback
        reconocerRol { resultado ->
            // Aquí puedes utilizar el valor de resultado
            //var rol = reconocerRol()
            //acceder a la colección  para descargar datos de la BD
            //recuperar datos de la bd -> la fecha se lee con getTimestamp
            val myBD= FirebaseFirestore.getInstance()
            val myCol = myBD.collection(resultado)
            // Obtener el correo electrónico ingresado por el usuario
            val emailToSearch = textEmail.text.toString()
            // Poner en mayusucla solo la primera letra a mayúsculas
            val emailPrimeraMayusucla = emailToSearch.substring(0, 1).toUpperCase() + emailToSearch.substring(1)
            myCol.document(emailPrimeraMayusucla).get().addOnSuccessListener {

                var nombre2 = if(it.get("nombre").toString() != "null") it.get("nombre").toString() else ""
                var apellido2 = if(it.get("apellidos").toString()!="null")
                    it.get("apellidos").toString() else "problemas"
                var telefono2 = if(it.get("telefono").toString()!="null") it.get("telefono").toString() else ""
                var fechaNacimiento2 = it.getTimestamp("fecha de nacimiento")

                //inyectarlas en los campos del formulario para mostrarlos en las etiquetas
                //las de tipo string, se pueden asignar directamente, solo hay que comprobar que no estén vacías
                //convertir timestamp a string

                textNombre.setText(nombre2)
                telefono.setText(telefono2)
                textApellido.setText(apellido2)

                //convertir timestamp a string
                if(fechaNacimiento2!=null) {
                    var fecha = fechaNacimiento2.toDate()
                    var formato = SimpleDateFormat("dd/MM/yyyy")
                    var fechastr = formato.format(fecha)
                    textFechaNacimiento.setText(fechastr)
                }

            }.addOnFailureListener{
                //showalert
                showAlert("Error al acceder al documento del usuario")
            }

        }

    }

    //////////////////////////////

    ///////////////////////////////////////////
    //recuerar al usuario que esta autentificado ahora mismo y guardarlo en una variable user
    private fun recuperar_userAutenticado(){

        val user = Firebase.auth.currentUser
        //let -> sirve para hacer varias acciones del tirón, ejecutar varias líneas de codigo
        user?.let { user ->
            //cambia el texto del campo del edittext
            //email es pk se hace con firebase
            // Obtener el correo electrónico de la bd (se obtiene todoo en minusculas)
            val emailToSearch = user.email.toString()
            // Poner en mayusucla solo la primera letra a mayúsculas
            val emailPrimeraMayusucla = emailToSearch.substring(0, 1).toUpperCase() + emailToSearch.substring(1)
            textEmail.setText(emailPrimeraMayusucla)
            //sirve para desactivar el edittext y que no se pueda introducir datos
            textEmail.isEnabled = false

        }
    }
    /////////////////////////////////////////////////////////////////
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
        val ok2= fechaRegex.matches(textFechaNacimiento.text.toString())
        if(!fechaRegex.matches(textFechaNacimiento.text.toString())){
            // Error
            Log.e("ERROR", "La fecha introducido no es correcto")
            textFechaNacimiento.error = "Fecha incorrecto"
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        val ok3= emailRegex.matches(textEmail.text.toString())
        if(!emailRegex.matches(textEmail.text.toString())){
            // Error
            Log.e("ERROR", "El formato de email introducido no es correcto")
            textEmail.error = "Email incorrecto"
        }

        val passwdRegex = Regex("^[A-Z](?=.*\\d).{7,}\$")
        val ok4= passwdRegex.matches(textContraseña.text.toString()) || textContraseña.text.toString() == ""
        if(!passwdRegex.matches(textContraseña.text.toString()) && textContraseña.text.toString()!=""){
            // Error
            Log.e("ERROR", "El formato de contraseña introducido no es correcto")
            textContraseña.error = "Contraseña incorrecta"
        }



        return ok1 && ok2 && ok3 && ok4

    }

    ///////////////////////////////////

    ///////////////////////////////////
    private fun almacenarInfoBD(){
        //convertir de string a timestamp para almacenarla
        //crear una instancia de tipo formato
        var formato = SimpleDateFormat("dd/MM/yyyy")
        // con ese tipo formato convertir a tipo date
        var fecha = formato.parse(textFechaNacimiento.text.toString())
        //convertir objeto date a objeto timestamp de firebase
        //esta es la variable que hay que almacenar -> usar el tipo de Firebase
        var timestamp= Timestamp(fecha)


        //definir un documento a mano en el sitio web de firebase -> cloud firestore
        //el substring es para que en la bd no se guarde los espacios de Sexo: Hombre. (el 6 es un código predefinido)
        var nuevaInfo = mapOf(
            "nombre" to textNombre.text.toString(),
            "apellidos" to textApellido.text.toString(),
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
        myCol.document(textEmail.text.toString()).set(nuevaInfo).addOnSuccessListener {
            //showalert
            showAlert("Registro del usuario correcto")

        }.addOnFailureListener{
            //showalert
            showAlert("Error en el registro del usuario")
        }

        // ACTUALIZAR CONTRASEÑA, si introduce una nueva.
        if(textContraseña.text.toString()!="") {
            val user = Firebase.auth.currentUser
            user!!.updatePassword(textContraseña.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "User password updated.")
                    }
                }
        }

    } //FIN almacenarInfoBD()


    ///////////////////////////////////
    /////////////////
    private fun logica_borrar(){
        btnEliminarCuenta.setOnClickListener {
            val myBD= FirebaseFirestore.getInstance()
            val myCol = myBD.collection("pacientes")
            // Obtener el correo electrónico ingresado por el usuario
            val emailToSearch = textEmail.text.toString()
            // Poner en mayusucla solo la primera letra a mayúsculas
            val emailPrimeraMayusucla = emailToSearch.substring(0, 1).toUpperCase() + emailToSearch.substring(1)
            val documentReference= myCol.document(emailPrimeraMayusucla)

            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_confirmacion_eliminar, null)
            val input = dialogView.findViewById<EditText>(R.id.editTextPalabra)
            builder.setView(dialogView)
            builder.setTitle("Confirmar eliminación")
            builder.setMessage("Para confirmar la eliminación, ingresa el email de la cuenta:")

            // Agregar botones de confirmación y cancelación
            builder.setPositiveButton("Sí") { dialog, _ ->
                val palabraIngresada = input.text.toString().trim()
                // Verificar si la palabra ingresada es correcta -> es el correo
                if (palabraIngresada == emailPrimeraMayusucla) {
                    // Usuario ingresó la palabra correcta, proceder con la eliminación
                    documentReference.delete()
                        .addOnSuccessListener {
                            // El documento se eliminó con éxito
                            showAlert("Se ha borrado con éxito")
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        .addOnFailureListener { exception ->
                            // Hubo un error al intentar eliminar el documento
                            Log.e("TAG", "Error al eliminar el documento")
                        }
                } else {
                    // Palabra ingresada incorrecta, puedes mostrar un mensaje o realizar otras acciones
                    showAlert("Palabra incorrecta. La eliminación no se llevó a cabo.")
                }
                dialog.dismiss() // Cerrar el cuadro de diálogo
            }
            builder.setNegativeButton("No") { dialog, _ ->
                // Usuario hizo clic en No, cancelar la eliminación
                dialog.dismiss() // Cerrar el cuadro de diálogo
            }

            // Mostrar el cuadro de diálogo
            val dialog = builder.create()
            dialog.show()
        }
    }

    //////////////////////


} //LLAVE FINAL