package com.us.app_gsti.pantallas.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import coil.load
import coil.size.Scale
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.modelo.Teleconsulta
import com.us.app_gsti.pantallas.modelo.TeleconsultaMedico
import com.us.app_gsti.pantallas.paciente.TeleconsultasPacienteActivity

class EditarTeleconsultaMedicoActivity : AppCompatActivity() {


    private lateinit var btn_volverAtras: Button
    private lateinit var comentario: EditText
    private lateinit var respuesta: EditText
    private lateinit var imagen: ImageView
    private lateinit var emailPaciente: EditText
    private lateinit var btnResponder: Button
    //
    private lateinit var teleconsultaMedico: TeleconsultaMedico

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_teleconsulta_medico)

        btn_volverAtras = findViewById(R.id.btn_volverAtras)
        btnResponder = findViewById(R.id.btnResponderTeleconsulta)

        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }

        comentario=findViewById(R.id.edittxt_comentario)
        respuesta=findViewById(R.id.edittxt_respuesta)
        emailPaciente=findViewById(R.id.txt_paciente)
        imagen=findViewById(R.id.imageViewTeleconsulta)

        //pasar info de la otra activity de tipo string
        //infoCita.text=getIntent().getStringExtra("especialidad") + "-" + getIntent().getStringExtra("id")

        //pasar info de la otra activity de tipo serializable -> sale tachado pk este método en versiones
        //posteriores de extra se borrará, han puesto otro alternativo
        teleconsultaMedico= getIntent().getSerializableExtra("teleconsulta") as TeleconsultaMedico
        comentario.setText(teleconsultaMedico.comentario)
        emailPaciente.setText(teleconsultaMedico.paciente)
        respuesta.setText(teleconsultaMedico.respuesta)
        imagen.load(teleconsultaMedico.imagenUrl) {
            crossfade(true)
            scale(Scale.FILL)
        }
        comentario.isEnabled = false
        emailPaciente.isEnabled = false

        btnResponder.setOnClickListener {
            val respuestaText = respuesta.text.toString()
            if (respuestaText.isNotEmpty()) {
                // Actualizar la teleconsulta del médico
                actualizarTeleconsultaMedico(respuestaText, teleconsultaMedico.id)
                actualizarTeleconsultaPaciente(respuestaText, teleconsultaMedico.id)

                intent = Intent(this, TeleconsultasMedicoActivity::class.java)
                startActivity(intent)
            } else {
                // Mostrar mensaje indicando que la respuesta no puede estar vacía
                Toast.makeText(this, "La respuesta no puede estar vacía", Toast.LENGTH_SHORT).show()
            }
        }


    }
    private fun actualizarTeleconsultaMedico(respuesta: String, idTeleconsultaMedico: String) {
        var email = Firebase.auth.currentUser!!.email.toString()
        var emailPrimeraMayusucla = email.substring(0, 1).toUpperCase() + email.substring(1)
        FirebaseFirestore.getInstance().collection("medicos")
            .document(emailPrimeraMayusucla).collection("teleconsultas").document(idTeleconsultaMedico)
            .update("respuesta", respuesta)
            .addOnSuccessListener {
                Toast.makeText(this, "Respuesta del médico actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar la respuesta del médico: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarTeleconsultaPaciente(respuesta: String, idTeleconsultaMedico: String) {
        var email = teleconsultaMedico.paciente
        FirebaseFirestore.getInstance().collection("pacientes")
            .document(email).collection("teleconsultas").document(idTeleconsultaMedico)
            .update("respuesta", respuesta)
            .addOnSuccessListener {
                Toast.makeText(this, "Respuesta del paciente actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar la respuesta del paciente: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }


}