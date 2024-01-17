package com.us.app_gsti.pantallas.paciente

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import coil.load
import coil.size.Scale
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.modelo.Teleconsulta

class EditarTeleconsultaActivity : AppCompatActivity() {

    private lateinit var btn_volverAtras: Button
    private lateinit var comentario: EditText
    private lateinit var respuesta: EditText
    private lateinit var imagen: ImageView
    private lateinit var emailMedico: EditText
    //
    private lateinit var teleconsulta: Teleconsulta

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_teleconsulta)

        btn_volverAtras = findViewById(R.id.btn_volverAtras)

        btn_volverAtras.setOnClickListener{
            // Manejar el clic de la flecha para ir a la pantalla anterior
            onBackPressedDispatcher.onBackPressed()
        }

        comentario=findViewById(R.id.edittxt_comentario)
        respuesta=findViewById(R.id.edittxt_respuesta)
        emailMedico=findViewById(R.id.txt_medico)
        imagen=findViewById(R.id.imageViewTeleconsulta)

        //pasar info de la otra activity de tipo string
        //infoCita.text=getIntent().getStringExtra("especialidad") + "-" + getIntent().getStringExtra("id")

        //pasar info de la otra activity de tipo serializable -> sale tachado pk este método en versiones
        //posteriores de extra se borrará, han puesto otro alternativo
        teleconsulta= getIntent().getSerializableExtra("teleconsulta") as Teleconsulta
        comentario.setText(teleconsulta.comentario)
        emailMedico.setText(teleconsulta.medico)
        respuesta.setText(teleconsulta.respuesta)
        imagen.load(teleconsulta.imagenUrl) {
            crossfade(true)
            scale(Scale.FILL)
        }
        comentario.isEnabled = false
        respuesta.isEnabled = false
        emailMedico.isEnabled = false
    }

}