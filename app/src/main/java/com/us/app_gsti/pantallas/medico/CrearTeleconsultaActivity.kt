package com.us.app_gsti.pantallas.medico

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.common.data.DataHolder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.InfoActivity
import com.us.app_gsti.pantallas.medico.CrearTeleconsultaActivity.Companion.IMAGE_CHOOSE
import com.us.app_gsti.pantallas.paciente.MapasPacienteActivity
import com.us.app_gsti.pantallas.paciente.TeleconsultasPacienteActivity
import java.io.ByteArrayOutputStream
import java.lang.System.currentTimeMillis

class CrearTeleconsultaActivity : AppCompatActivity() {

    private lateinit var textEmailMedico: EditText
    private lateinit var textEmailPaciente: EditText
    private lateinit var textAdjuntarImagen: EditText
    private lateinit var imViewTeleconsulta: ImageView
    private lateinit var btnClip: Button
    private var urlImagen : String = ""
    private lateinit var storage: FirebaseStorage
    private lateinit var textComentario: EditText
    private lateinit var btnCrearTeleconsulta: Button
    private lateinit var btn_volverAtras: Button


    companion object {
        const val IMAGE_CHOOSE = 1
        const val REQUEST_GALLERY_CODE = 3
    }



    override fun onCreate(savedInstanceState: Bundle?) {

        //-----------------------------------------------------------------------------------------------------
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_teleconsulta)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


        //--------------------------------------------------------------------------Inicialización de las variables
        textEmailMedico = findViewById(R.id.txt_medico)
        textEmailPaciente = findViewById(R.id.txt_paciente)

        //----------------------------------------------------------------------Variables relacionadas con las imágenes
        textAdjuntarImagen = findViewById(R.id.editTextAjuntarImagen)
        textAdjuntarImagen.isEnabled = false


        btnClip = findViewById(R.id.btn_clip)
        btnClip.setOnClickListener{
            logica_btnClip()
        }

        imViewTeleconsulta = findViewById(R.id.imageViewTeleconsulta)

        textComentario = findViewById(R.id.editTextComentario)
        btnCrearTeleconsulta = findViewById(R.id.btnCrearTeleconsulta)
        btn_volverAtras = findViewById(R.id.btn_volverAtras)

        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }

        logica_crearTeleconsulta()
        recuperar_userAutenticado()

        //-------------------------------------------------------------------------------
        storage = Firebase.storage

    }
    ///////////////////////////////////////////////////////////fin del onCreate


    /////////////////////Lógica de botones

    //------------------------------------------------------------------------------------------
    private fun logica_crearTeleconsulta(){
        btnCrearTeleconsulta.setOnClickListener{
            //Comprobar que los datos son correctos
            if(validacionDatos()){
                //COMPROBAR QUE ESTA EN LA BD
                val myBD= FirebaseFirestore.getInstance()
                val teleconsultaId = myBD.collection("teleconsultas").document().id
                val myCol = myBD.collection("medicos")
                // Obtener el correo electrónico del medico ingresado por el paciente
                val emailMedico = textEmailMedico.text.toString()
                // Poner en mayuscula solo la primera letra a mayúsculas
                myCol.document(emailMedico).get().addOnSuccessListener { document ->
                    //SI ESTÁ EN LA BD, hay documento
                    if (document.exists() && document.data != null) {
                        subirImagen{
                            Log.d("Debug", "el url guardado es"+urlImagen)
                            almacenarInfoBDMedico(teleconsultaId)
                            almacenarInfoBDPaciente(teleconsultaId)
                            intent = Intent(this, TeleconsultasPacienteActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Teleconsulta creada correctamente", Toast.LENGTH_SHORT).show()
                        }
                    }else {
                        Log.e("ERROR", "El email del medico no existe en la BD")
                        textEmailMedico.error = "Email no está registrado en la BD"
                    }
                }.addOnFailureListener{
                    Log.e("ERROR", "Error al buscar el email del medico en la BD")
                }
            }
        }
    }

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

        return ok3 && ok4

    }

    private fun almacenarInfoBDMedico(teleconsultaId: String){
        //definir un documento a mano en el sitio web de firebase -> cloud firestore
        //el substring es para que en la bd no se guarde los espacios de Sexo: Hombre. (el 6 es un código predefinido)
        var nuevaInfoMedico = mapOf(
            "emailPaciente" to textEmailPaciente.text.toString(),
            "urlImagen" to urlImagen,
            "comentario" to textComentario.text.toString(),
            "respuesta" to "",
        )
        //colección: "medicos"
        //id del documento -> email del medico autentificado
        //acceder a la bd -> usarlo siempre para introducir o extraer datos
        val myBD = FirebaseFirestore.getInstance()
        //si no esta creada la coleccion la crea y si no, accede
        val myCol = myBD.collection("medicos")
        //guardar en la bd, en la colección medicos, un documento
        //email es el id, en el set se le pasa el documento
        myCol.document(textEmailMedico.text.toString()).collection("teleconsultas")
            .document(teleconsultaId).set(nuevaInfoMedico)
            .addOnSuccessListener {
            //showalert
        }
        .addOnFailureListener{
            //showalert
            showAlert("Error en el registro de la teleconsulta")
        }

    } //FIN almacenarInfoBDMedico()

    private fun almacenarInfoBDPaciente(teleconsultaId: String){

        //definir un documento a mano en el sitio web de firebase -> cloud firestore
        //el substring es para que en la bd no se guarde los espacios de Sexo: Hombre. (el 6 es un código predefinido)
        var nuevaInfoPaciente = mapOf(
            "emailMedico" to textEmailMedico.text.toString(),
            "urlImagen" to urlImagen,
            "comentario" to textComentario.text.toString(),
            "respuesta" to ""

        )
        //colección: "pacientes"
        //id del documento -> email del medico autentificado
        //acceder a la bd -> usarlo siempre para introducir o extraer datos
        val myBD = FirebaseFirestore.getInstance()
        //si no esta creada la coleccion la crea y si no, accede
        val myCol = myBD.collection("pacientes")
        //guardar en la bd, en la colección medicos, un documento
        //email es el id, en el set se le pasa el documento
        myCol.document(textEmailPaciente.text.toString()).collection("teleconsultas")
            .document(teleconsultaId).set(nuevaInfoPaciente).addOnSuccessListener {
            //showalert
        }.addOnFailureListener{
            //showalert
            showAlert("Error en el registro de la teleconsulta")
        }

    } //FIN almacenarInfoBDMedico()

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
            textEmailPaciente.setText(emailPrimeraMayusucla)
            //sirve para desactivar el edittext y que no se pueda introducir datos
            textEmailPaciente.isEnabled = false

        }
    }

    //--------------------------------------------------------------------------------

    private fun logica_btnClip(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                requestGalleryPermissions()
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_GALLERY_CODE
                )
            } else{
                chooseImageGallery()
            }
        }else{
            chooseImageGallery()
        }
    }

    private fun requestGalleryPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_MEDIA_IMAGES)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            //si no han sido pedidos aun --> los pide
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_GALLERY_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_GALLERY_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImageGallery()
                } else {
                    Toast.makeText(this, "Permisos de galería deshabilitados", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "Ha ocurrido una excepción", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK){
            imViewTeleconsulta.setImageURI(data?.data)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //-------------------------------------------------------------------------------------------------


    private fun subirImagen(callback: () -> Unit) {
        Log.d("Debug", "Entra en subir imagen")
        val storageRef = storage.reference
        val rutaImagen = storageRef.child("pacientes/" + Firebase.auth.currentUser?.email.toString() + "/teleconsulta-" + currentTimeMillis() + ".jpeg")

        val drawable = imViewTeleconsulta.drawable
        val bitmap = if (drawable is BitmapDrawable) {
            Log.d("Debug", "if")
            drawable.bitmap
        } else {
            // Manejar el caso en que el drawable no es un BitmapDrawable
            showAlert("No ha introducido una imagen o el tipo de imagen es incompatible")
            //Toast.makeText(this, "No ha introducido una imagen o el tipo de imagen es incompatible", Toast.LENGTH_SHORT).show()
            Log.d("Debug", "No ha introducido una imagen o el tipo de imagen es incompatible")
            return
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()

        val uploadTask = rutaImagen.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this, "Error en la subida de la imagen", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            rutaImagen.downloadUrl.addOnSuccessListener {
                Log.v("STORAGE", "LA URL ES DE LA IMAGEN ES:" + it)
                urlImagen = it.toString()
                Log.v("STORAGE", "LA URL ALMACENADA ES:" + urlImagen)
                callback()
            }
        }

    }


}