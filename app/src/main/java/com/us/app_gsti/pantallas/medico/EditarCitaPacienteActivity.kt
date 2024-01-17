package com.us.app_gsti.pantallas.medico

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.modelo.Cita
import java.text.SimpleDateFormat

class EditarCitaPacienteActivity : AppCompatActivity() {

    private lateinit var botonEliminarCita: Button
    private lateinit var btn_volverAtras: Button
    private lateinit var consulta: EditText
    private lateinit var fechaCita: EditText
    private lateinit var emailPaciente: EditText
    private lateinit var centroHospitalario: EditText
    //
    private lateinit var cita: Cita

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_cita_paciente)

        btn_volverAtras = findViewById(R.id.btn_volverAtras)

        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }

        botonEliminarCita=findViewById(R.id.btn_cancelar)
        consulta=findViewById(R.id.txt_consulta)
        fechaCita=findViewById(R.id.edittext_fecha)
        emailPaciente=findViewById(R.id.txt_paciente)
        centroHospitalario=findViewById(R.id.edittxt_hospital)

        //pasar info de la otra activity de tipo string
        //infoCita.text=getIntent().getStringExtra("especialidad") + "-" + getIntent().getStringExtra("id")

        //pasar info de la otra activity de tipo serializable -> sale tachado pk este método en versiones
        //posteriores de extra se borrará, han puesto otro alternativo
        cita = getIntent().getSerializableExtra("cita") as Cita
        fechaCita.setText(SimpleDateFormat("dd/MM/yyyy HH:mm").format(cita.fecha.time).toString())
        consulta.setText(cita.consulta)
        emailPaciente.setText(cita.paciente)
        centroHospitalario.setText(cita.hospital)

        fechaCita.isEnabled = false
        consulta.isEnabled = false
        emailPaciente.isEnabled = false
        centroHospitalario.isEnabled = false

        botonEliminarCita.setOnClickListener{(eliminarCita())}

    }


    private fun eliminarCita() {
        if (botonEliminarCita.text == "Cancelar cita") {
            // Acceder a la BD y buscar cita con el ID -> .delete().addOnSuccessListener{...}
            var email = Firebase.auth.currentUser!!.email.toString()
            var emailPrimeraMayusucla = email.substring(0, 1).toUpperCase() + email.substring(1)

            FirebaseFirestore.getInstance().collection("medicos")
                .document(emailPrimeraMayusucla).collection("citas").document(cita.id)
                .delete().addOnSuccessListener {
                    showAlert("Se ha cancelado correctamente la cita correspondiente al paciente " + cita.paciente + " con día y hora " + SimpleDateFormat("dd/MM/yyyy HH:mm").format(cita.fecha.time).toString())
                }
        } else {
            startActivity(Intent(this, CitasMedicoActivity::class.java))
        }
    }

    private fun showAlert(text: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Paciente eliminado correctamente")
            .setMessage(text)
            .setPositiveButton("ACEPTAR") { dialog, which ->
                // Acción a realizar cuando se presiona "ACEPTAR"
                startActivity(Intent(this, CitasMedicoActivity::class.java))
            }

        val dialogo: AlertDialog = builder.create()
        dialogo.show()
    }

}
