package com.us.app_gsti.pantallas.medico

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.modelo.SeleccionFecha
import com.us.app_gsti.pantallas.modelo.SeleccionHora
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CrearCitaActivity : AppCompatActivity() {

    //hay que poner el correo aqui
    private lateinit var textEmailMedico: EditText
    private lateinit var textEmailPaciente: EditText
    private lateinit var editTextConsulta: EditText
    private lateinit var editTextHora: EditText
    private lateinit var btn_fecha: Button
    private lateinit var btnCrear: Button
    private lateinit var lista_hospitales: Spinner
    private lateinit var btn_volverAtras: Button

    //variable global accesible desde cualquier funcion de la clase
    private var storedTimestamp: Timestamp? = null

    // Lista de opciones
    private val hospitales =
        arrayOf("Selecciona hospital", "VIRGEN DEL ROCIO", "VIRGEN MACARENA")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cita)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //------------------------------------------------------------------------------------------

        btn_volverAtras = findViewById(R.id.btn_volverAtras)

        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }

        //------------------------------------------------------------------------------------------

        //inicializa las variables

        textEmailPaciente=findViewById(R.id.txt_paciente)
        textEmailMedico= findViewById(R.id.txt_medico)
        editTextConsulta= findViewById(R.id.txt_consulta)
        btn_fecha= findViewById(R.id.btn_fecha)
        btnCrear= findViewById(R.id.btnCrearCita)
        editTextHora= findViewById(R.id.txt_hora)
        // Parte grafica del spinner
        lista_hospitales = findViewById(R.id.spinner_hospital)

        //falta hospital, departamento  y hora
        creaAdaptador()
        lista_hospitales.onItemSelectedListener = SpinnerActivity()


        editTextHora.isEnabled = false

        btn_fecha.setOnClickListener{seleccionarFechaHora()}
        recuperar_userAutenticado()

        logica_crearCita()

    }

    /////////////////
    private fun seleccionarFechaHora(){

        val datePicker = SeleccionFecha { day, moth, year ->
            // Después de seleccionar la fecha, abre el selector de hora
            val timePicker= SeleccionHora {hour, minute ->
                logicaFecha(day, moth, year, hour, minute)
            }
            timePicker.show(supportFragmentManager, "timePicker")
        }
        datePicker.show(supportFragmentManager, "datePicker")


    }
    ///////////////////

    private fun logicaFecha(d: Int, m:Int, y:Int, h: Int, mi:Int) {
        // Obtén la fecha actual
        val c = Calendar.getInstance()
        // Establece la hora y minuto, dias años... seleccionados
        c.set(Calendar.DAY_OF_YEAR, d)
        c.set(Calendar.MONTH, m)
        c.set(Calendar.YEAR, y)
        c.set(Calendar.HOUR_OF_DAY, h)
        c.set(Calendar.MINUTE, mi)
        // Obtén la fecha actual
        val fechaActual = Calendar.getInstance().time
        // Obtiene la fecha seleccionada
        val fechaSeleccionada = c.time
        // Verifica si la fecha seleccionada es posterior a la fecha actual
        if (fechaSeleccionada.compareTo(fechaActual) > 0) {
            // Crea un formato de fecha
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            // Convierte el Calendar a un objeto Date
            val fecha = c.time
            // Convierte la fecha a un string para mostrarla o almacenarla
            val fechaString = formato.format(fecha)
            // Muestra la fecha y hora seleccionadas en un EditText o donde desees
            showAlert(fechaString)
            // Convierte la fecha a un Timestamp si es necesario
            val timeStamp = Timestamp(fecha)
            editTextHora.setText(fecha.toString())
            editTextHora.isEnabled = false
            // Puedes almacenar o utilizar el timeStamp según tus necesidades
            storedTimestamp = timeStamp
        }else{
            //si la fecha seleccionada es posterior -> como una cita no puede ser en el pasado
            editTextHora.setText("La fecha debe ser posterior a la actual")
            editTextHora.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            Log.e("ERROR", "La fecha debe ser posterior a la actual")
            editTextHora.error = "La fecha debe ser posterior a la actual"
        }
    }

    ////////////////////////////////////////////


    private fun logica_crearCita(){
        btnCrear.setOnClickListener {
            if (validacionDatos()) {
                //COMPROBAR QUE ESTA EN LA BD
                val myBD= FirebaseFirestore.getInstance()
                val myCol = myBD.collection("pacientes")
                // Obtener el correo electrónico del paciente ingresado por el medico
                val emailPaciente = textEmailPaciente.text.toString()
                // Poner en mayuscula solo la primera letra a mayúsculas
                myCol.document(emailPaciente).get().addOnSuccessListener { document ->
                    //SI ESTÁ EN LA BD, hay documento
                    if (document.exists() && document.data != null) {
                        almacenarInfoBDMedico()
                        almacenarInfoBDPaciente()

                        intent = Intent(this, CitasMedicoActivity::class.java)
                        startActivity(intent)
                    }else {
                        Log.e("ERROR", "El email del paciente no existe en la BD")
                        textEmailPaciente.error = "Email no está registrado en la BD"
                    }
                }.addOnFailureListener{
                    Log.e("ERROR", "Error al buscar el email del paciente en la BD")
                }

            }

        }
    }


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
            textEmailMedico.setText(emailPrimeraMayusucla)
            //sirve para desactivar el edittext y que no se pueda introducir datos
            textEmailMedico.isEnabled = false

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

        //Comprobar expresión regular de fecha: dd/MM/aaaa


        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
        val ok3= emailRegex.matches(textEmailMedico.text.toString())
        if(!emailRegex.matches(textEmailMedico.text.toString())){
            // Error
            Log.e("ERROR", "El formato de email de medico introducido no es correcto")
            textEmailMedico.error = "Email incorrecto"
        }

        val ok4= emailRegex.matches(textEmailPaciente.text.toString())
        if(!emailRegex.matches(textEmailPaciente.text.toString())){
            // Error
            Log.e("ERROR", "El formato de email del paciente introducido no es correcto")
            textEmailPaciente.error = "Email incorrecto"
        }

        val ok5= editTextConsulta.text.isNotBlank()
        if(!ok5){
            // Error
            Log.e("ERROR", "Por favor, ingresa el número de consulta antes de crear la cita.")
            editTextConsulta.error = "Número de consulta vacía"
        }
        val ok6= storedTimestamp != null
        if(!ok6){
            // Error
            Log.e("ERROR", "Por favor, ingresa la fecha/hora antes de crear la cita.")
            editTextHora.error = "Fecha vacía"
        }

        return ok3 && ok4 && ok5 && ok6

    }

    ///////////////////////////////////

    ///////////////////////////////////
    private fun almacenarInfoBDPaciente(){

        /*
        //convertir de string a timestamp para almacenarla
        //crear una instancia de tipo formato
        var formato = SimpleDateFormat("dd/MM/yyyy")
        // con ese tipo formato convertir a tipo date
        var fecha = formato.parse(editTextFecha.text.toString())
        //convertir objeto date a objeto timestamp de firebase
        //esta es la variable que hay que almacenar -> usar el tipo de Firebase
        var timestamp= Timestamp(fecha)


         */
        //definir un documento a mano en el sitio web de firebase -> cloud firestore
        //el substring es para que en la bd no se guarde los espacios de Sexo: Hombre. (el 6 es un código predefinido)
        var nuevaInfoPaciente = mapOf(
            "emailMedico" to textEmailMedico.text.toString(),
            "fecha y hora de cita" to storedTimestamp,
            "centro hospitalario" to lista_hospitales.selectedItem.toString(),
            "Numero de consulta" to editTextConsulta.text.toString()
        )

        //colección: "paciente"
        //id del documento -> email del paciente autentificado
        //acceder a la bd -> usarlo siempre para introducir o extraer datos
        val myBD = FirebaseFirestore.getInstance()
        //si no esta creada la coleccion la crea y si no, accede
        val myCol = myBD.collection("pacientes")
        //guardar en la bd, en la colección pacientes, un documento
        //email es el id, en el set se le pasa el documento
        myCol.document(textEmailPaciente.text.toString()).collection("citas").add(nuevaInfoPaciente).addOnSuccessListener {
            //showalert
            showAlert("Registro de la cita correcto")

        }.addOnFailureListener{
            //showalert
            showAlert("Error en el registro de la cita")
        }

    } //FIN almacenarInfoBDPaciente()

    private fun almacenarInfoBDMedico(){

        /*
        //convertir de string a timestamp para almacenarla
        //crear una instancia de tipo formato
        var formato = SimpleDateFormat("dd/MM/yyyy")
        // con ese tipo formato convertir a tipo date
        var fecha = formato.parse(editTextFecha.text.toString())
        //convertir objeto date a objeto timestamp de firebase
        //esta es la variable que hay que almacenar -> usar el tipo de Firebase
        var timestamp= Timestamp(fecha)


         */
        //definir un documento a mano en el sitio web de firebase -> cloud firestore
        //el substring es para que en la bd no se guarde los espacios de Sexo: Hombre. (el 6 es un código predefinido)
        var nuevaInfoMedico = mapOf(
            "emailPaciente" to textEmailPaciente.text.toString(),
            "fecha y hora de cita" to storedTimestamp,
            "centro hospitalario" to lista_hospitales.selectedItem.toString(),
            "Numero de consulta" to editTextConsulta.text.toString()
        )
        //colección: "paciente"
        //id del documento -> email del paciente autentificado
        //acceder a la bd -> usarlo siempre para introducir o extraer datos
        val myBD = FirebaseFirestore.getInstance()
        //si no esta creada la coleccion la crea y si no, accede
        val myCol = myBD.collection("medicos")
        //guardar en la bd, en la colección pacientes, un documento
        //email es el id, en el set se le pasa el documento
        myCol.document(textEmailMedico.text.toString()).collection("citas").add(nuevaInfoMedico).addOnSuccessListener {
            //showalert
            showAlert("Registro de la cita correcto")

        }.addOnFailureListener{
            //showalert
            showAlert("Error en el registro de la cita")
        }

    } //FIN almacenarInfoBDPaciente()

    //////////////////////

    //VAMOS A CREAR UN SPINNER -> INFO EN LA ENSEÑANZA
    fun creaAdaptador() { //El adaptador mete los elementos en la lista
        var adapt = ArrayAdapter(this, android.R.layout.simple_spinner_item, hospitales)
        adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lista_hospitales.adapter = adapt
    }
    ////////////////////////
//listener -> relacionar acciones a cada boton
    inner class SpinnerActivity : Activity(),
        AdapterView.OnItemSelectedListener { //Inner class es una clase dentro de otra clase
        //Se podria crear a parte pero como solo se va a usar en esta clase po no pasa na
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

            // pos se corresponde con la posicion en el array. Si pulso en el 0 se hace el boton invisible
            if (pos != 0) {
                btnCrear.visibility = View.VISIBLE
            } else {
                btnCrear.visibility = View.INVISIBLE
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Another interface callback
        }
    }
    //////////////////////////////////////

} //llave final