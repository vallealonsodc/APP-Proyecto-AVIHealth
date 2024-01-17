package com.us.app_gsti.pantallas.medico

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.CabeceraMedico
import com.us.app_gsti.pantallas.modelo.Teleconsulta
import com.us.app_gsti.pantallas.modelo.TeleconsultaMedico
import com.us.app_gsti.pantallas.paciente.EditarTeleconsultaActivity


class TeleconsultasMedicoActivity : CabeceraMedico() {

    private lateinit var btnCitas: Button
    private lateinit var btnMapas: Button
    private lateinit var lista_teleconsultas: RecyclerView
    private lateinit var lista_teleconsultas_respondidas: RecyclerView
    private var isDropdownOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teleconsultas_medico)

        lista_teleconsultas= findViewById(R.id.recyclerView_teleconsultas)
        lista_teleconsultas_respondidas= findViewById(R.id.recyclerView_teleconsultas_respondidas)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

//--------------------------------------------------------------------------------------------------

        val teleconsultasButton = findViewById<Button>(R.id.btn_teleconsulta)

        // Cambiar el color del icono cuando estas en esta activity
        val blueColor = ContextCompat.getColor(this, R.color.blue) // Replace with your actual blue color resource
        teleconsultasButton?.compoundDrawables?.get(1)?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(blueColor, BlendModeCompat.SRC_ATOP)

//--------------------------------------------------------------------------------------------------

        btnCitas = findViewById(R.id.btn_citas)
        btnCitas.setOnClickListener{
            intent = Intent(this,  CitasMedicoActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, R.transition.fade_in, R.transition.fade_out
            )

            startActivity(intent, options.toBundle())
        }

        btnMapas = findViewById(R.id.btn_mapa)
        btnMapas.setOnClickListener{
            intent = Intent(this,  MapasMedicoActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, R.transition.fade_in, R.transition.fade_out
            )

            startActivity(intent, options.toBundle())
        }

        val btnDesplegable = findViewById<Button>(R.id.btn_settings)
        btnDesplegable.setOnClickListener { view ->
            showMenuMedico(view)
        }
        muestraTeleconsultas()


    }

    fun muestraTeleconsultas(){


        //acceder a la bd a la coleccion citas dek paciente autentificado
        var email = Firebase.auth.currentUser!!.email.toString()
        var emailPrimeraMayusucla = email.substring(0, 1).toUpperCase() + email.substring(1)
        var teleconsultas= mutableListOf<TeleconsultaMedico>()
        var teleconsultasRespondidas= mutableListOf<TeleconsultaMedico>()

        FirebaseFirestore.getInstance().collection("medicos")
            .document(emailPrimeraMayusucla).collection("teleconsultas").get().addOnSuccessListener {documentos ->
                //para cada documento (para cada cita) de el paciente logueado
                for(doc in documentos){
                    //obtendo los parámetros de las citas de la bd
                    //el id se hace de una forma especial -> no es un atrinuto, sino es el identificador del doc
                    var id= doc.id
                    //la estructura de la bd (documentos...) se ha hecho directamente con la app
                    var paciente= doc.get("emailPaciente").toString()
                    var comentario= doc.get("comentario").toString()
                    val imagenUrl = doc.get("urlImagen").toString() // Asegúrate de que este es el campo correcto en Firestore
                    val respuesta = doc.get("respuesta").toString()
                    if (respuesta == "") {
                        // Add to the list of teleconsultas without a response
                        teleconsultas.add(TeleconsultaMedico(id, paciente, comentario, imagenUrl, respuesta))
                    } else {
                        // Add to the list of responded teleconsultas
                        teleconsultasRespondidas.add(TeleconsultaMedico(id, paciente, comentario, imagenUrl, respuesta))
                    }

                }

                var adaptador = AdaptadorTeleconsultas(teleconsultas){ c-> editarTeleconsultaMedico(c)}
                //false para que se muestre en orden inverso
                //definir como quiero q se meuestren
                lista_teleconsultas.layoutManager= LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL,false)
                //añadirlo a cyrcleview
                lista_teleconsultas.adapter=adaptador

                var adaptadorRespondidas = AdaptadorTeleconsultas(teleconsultasRespondidas) { c -> editarTeleconsultaMedico(c) }
                lista_teleconsultas_respondidas.layoutManager = LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL, false)
                lista_teleconsultas_respondidas.adapter = adaptadorRespondidas
            }

    }// FIN muestraCitas()

    //redirige al activity
    private fun editarTeleconsultaMedico(c: TeleconsultaMedico){
        val intent = Intent(this, EditarTeleconsultaMedicoActivity::class.java)
        //pasar info String de una activity a otra
        //intent.putExtra("especialidad", c.especialidad)
        //intent.putExtra("id", c.id)

        //pasar objetos de activities -> tienen que ser serialisable
        intent.putExtra("teleconsulta",c)
        startActivity(intent)
    }

    //los datos que queremos sacar en la tabla (parámetro)
    //el adaptador recibe los datos
    //se le definen dos atributos a la clase, una lista y una funcion que recibe una cita y no devuelve nada
    inner class AdaptadorTeleconsultas(private var dataSet: List<TeleconsultaMedico>,
                                       private var clickTeleconsulta: (TeleconsultaMedico)-> Unit) :
        RecyclerView.Adapter<AdaptadorTeleconsultas.TeleconsultaViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        //define el formato de las filas
        inner class TeleconsultaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            //campos de las filas de las tablas
            val comentario: TextView
            val paciente: TextView
            val imageViewFlecha: ImageView = view.findViewById(R.id.arrowImageView)
            val imageViewTeleconsulta: ImageView = view.findViewById(R.id.imageViewTeleconsulta)
            val comentario2: TextView = view.findViewById(R.id.textComentario2)
            val detallesLayout: LinearLayout = view.findViewById(R.id.LinearLayoutTeleconsulta)
            //val imagen: TextView
            init {
                // constructor de la clase interna
                paciente = view.findViewById(R.id.textPaciente)
                comentario = view.findViewById(R.id.textComentario)
                //imagen = view.findViewById(R.id.imageView)
                paciente.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val teleconsultaMedico = dataSet[position]
                        teleconsultaMedico.isExpanded = !teleconsultaMedico.isExpanded
                        notifyItemChanged(position)
                    }
                }
                imageViewFlecha.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val teleconsultaMedico = dataSet[position]
                        teleconsultaMedico.isExpanded = !teleconsultaMedico.isExpanded
                        notifyItemChanged(position)
                    }
                }
            }
        }

        // Create new views (invoked by the layout manager)
        //clase adaptador
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TeleconsultaViewHolder {
            // Create a new view, which defines the UI of the list item

            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_teleconsulta_medico, viewGroup, false)
            return TeleconsultaViewHolder(view)
        }
        // Replace the contents of a view (invoked by the layout manager)
        //parametro posicion, posicion de cada una de las filas de la tabla
        //método que muestra la informacion en cada una de las listas de la tabla
        override fun onBindViewHolder(viewHolder: TeleconsultaViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            //viewHolder.imagen.text = dataSet[position].imagen
            val teleconsultaMedico = dataSet[position]
            viewHolder.paciente.text = teleconsultaMedico.paciente
            viewHolder.comentario.visibility = if (teleconsultaMedico.isExpanded) View.VISIBLE else View.GONE
            viewHolder.comentario.text = if (teleconsultaMedico.respuesta == "") teleconsultaMedico.comentario else teleconsultaMedico.respuesta
            viewHolder.imageViewTeleconsulta.visibility = if (teleconsultaMedico.isExpanded) View.VISIBLE else View.GONE
            viewHolder.comentario2.visibility = if (teleconsultaMedico.isExpanded) View.VISIBLE else View.GONE
            viewHolder.comentario2.text = if (teleconsultaMedico.respuesta == "") "Comentario" else "Respuesta"
            viewHolder.imageViewTeleconsulta.load(teleconsultaMedico.imagenUrl) {
                crossfade(true)
                scale(Scale.FILL)
            }
            val arrowImageResId = if (teleconsultaMedico.isExpanded) {
                R.drawable.angulo_pequeno_hacia_arriba // Reemplaza con tu recurso de flecha hacia arriba
            } else {
                R.drawable.angulo_pequeno_hacia_abajo // Reemplaza con tu recurso de flecha hacia abajo
            }
            viewHolder.imageViewFlecha.setImageResource(arrowImageResId)

            // Adjust the layout parameters if necessary
            val layoutParams = viewHolder.detallesLayout.layoutParams
            layoutParams.height = if (teleconsultaMedico.isExpanded) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.WRAP_CONTENT
            viewHolder.detallesLayout.layoutParams = layoutParams


            //itemView es la fila de la tabla
            //fucnionalidad para poder editar citas
            //datasetCita -> es la posicion en la lista de citas -> osea una cita
            viewHolder.itemView.setOnClickListener{(clickTeleconsulta(dataSet[position]))}
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }

}