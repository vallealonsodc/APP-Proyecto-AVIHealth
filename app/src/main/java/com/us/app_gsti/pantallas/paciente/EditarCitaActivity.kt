package com.us.app_gsti.pantallas.paciente

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.medico.CitasMedicoActivity
import com.us.app_gsti.pantallas.modelo.CitaPaciente
import java.text.SimpleDateFormat

class EditarCitaActivity : AppCompatActivity() {
    private lateinit var btn_volverAtras: Button
    private lateinit var consulta: EditText
    private lateinit var fechaCita: EditText
    private lateinit var emailMedico: EditText
    private lateinit var centroHospitalario: EditText
    //
    private lateinit var cita: CitaPaciente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_cita)

        btn_volverAtras = findViewById(R.id.btn_volverAtras)

        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }

        consulta=findViewById(R.id.txt_consulta)
        fechaCita=findViewById(R.id.edittext_fecha)
        emailMedico=findViewById(R.id.txt_medico)
        centroHospitalario=findViewById(R.id.edittxt_hospital)

        //pasar info de la otra activity de tipo string
        //infoCita.text=getIntent().getStringExtra("especialidad") + "-" + getIntent().getStringExtra("id")

        //pasar info de la otra activity de tipo serializable -> sale tachado pk este método en versiones
        //posteriores de extra se borrará, han puesto otro alternativo
        cita= getIntent().getSerializableExtra("cita") as CitaPaciente
        fechaCita.setText(SimpleDateFormat("dd/MM/yyyy HH:mm").format(cita.fecha.time).toString())
        consulta.setText(cita.consulta)
        emailMedico.setText(cita.medico)
        centroHospitalario.setText(cita.hospital)

        fechaCita.isEnabled = false
        consulta.isEnabled = false
        emailMedico.isEnabled = false
        centroHospitalario.isEnabled = false
    }

}