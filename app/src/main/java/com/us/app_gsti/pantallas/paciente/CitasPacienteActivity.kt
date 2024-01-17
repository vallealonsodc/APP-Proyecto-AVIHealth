package com.us.app_gsti.pantallas.paciente

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.us.app_gsti.R
import com.us.app_gsti.pantallas.CabeceraPaciente
import com.us.app_gsti.pantallas.modelo.CitaPaciente
import java.text.SimpleDateFormat
import java.util.Calendar

class CitasPacienteActivity : CabeceraPaciente() {

    private lateinit var btnTeleconsultas: Button
    private lateinit var btnMapas: Button
    private lateinit var lista_citas: RecyclerView
    private lateinit var adaptador: AdaptadorCitas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_citas_paciente)

        lista_citas= findViewById(R.id.recyclerView_citas)

        // Este código sirve para que la barra de navigationBar (la de abajo que te permite volver atras o salir de la app) se oculte.
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Ocultar la barra de navegación y la barra de estado.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Configurar el comportamiento para que la barra de navegación se muestre temporalmente con un gesto de deslizamiento.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        //------------------------------------------------------------------------------------------

        val citasButton = findViewById<Button>(R.id.btn_citas)

        // Cambiar el color del icono cuando estas en esta activity
        val blueColor = ContextCompat.getColor(this, R.color.blue) // Replace with your actual blue color resource
        citasButton?.compoundDrawables?.get(1)?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(blueColor, BlendModeCompat.SRC_ATOP)

        //------------------------------------------------------------------------------------------

        btnTeleconsultas = findViewById(R.id.btn_teleconsulta)
        btnTeleconsultas.setOnClickListener{
            intent = Intent(this,  TeleconsultasPacienteActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, R.transition.fade_in, R.transition.fade_out
            )

            startActivity(intent, options.toBundle())
        }

        btnMapas = findViewById(R.id.btn_mapa)
        btnMapas.setOnClickListener{
            intent = Intent(this,  MapasPacienteActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, R.transition.fade_in, R.transition.fade_out
            )

            startActivity(intent, options.toBundle())
        }

        val btnDesplegable = findViewById<Button>(R.id.btn_settings_paciente)
        btnDesplegable.setOnClickListener { view ->
            showMenuPaciente(view)
        }

        muestraCitas()

        val editTextBuscarCitas: EditText = findViewById(R.id.edittxt_buscar_citas)

        editTextBuscarCitas.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                true
            } else {
                false
            }
        }

        editTextBuscarCitas.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario implementarlo
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No es necesario implementarlo
            }

            override fun afterTextChanged(s: Editable?) {
                // Llamamos al método de filtro aquí
                adaptador?.filter(s.toString())
            }
        })

        //------------------------------------------------------------------------------------------


    }
    fun muestraCitas(){


        //acceder a la bd a la coleccion citas dek paciente autentificado
        var email = Firebase.auth.currentUser!!.email.toString()
        var emailPrimeraMayusucla = email.substring(0, 1).toUpperCase() + email.substring(1)
        var citas= mutableListOf<CitaPaciente>()

        FirebaseFirestore.getInstance().collection("pacientes")
            .document(emailPrimeraMayusucla).collection("citas").get().addOnSuccessListener {documentos ->
                //para cada documento (para cada cita) de el paciente logueado
                for(doc in documentos){
                    //obtendo los parámetros de las citas de la bd
                    //el id se hace de una forma especial -> no es un atrinuto, sino es el identificador del doc
                    var id= doc.id
                    //los signos de exclamación !! -> sirven para
                    //sacar fecha de la bd y transformarlo a tipo calendar
                    var fecha= doc.getTimestamp("fecha y hora de cita")!!.toDate()
                    var cal= Calendar.getInstance()
                    cal.time=fecha
                    //la estructura de la bd (documentos...) se ha hecho directamente con la app
                    var consulta= doc.get("Numero de consulta").toString()
                    var medico= doc.get("emailMedico").toString()
                    var hospital= doc.get("centro hospitalario").toString()
                    //guardo la cita en la lista
                    //creo un objeto cita
                    citas.add(CitaPaciente(id, consulta, cal, medico, hospital))

                }

                citas.sortWith(compareBy { it.fecha }) // Ordena la lista por fecha
                //crear adaptador y lo configuramos
                //se vincula el addaptador con los atos
                adaptador = AdaptadorCitas(citas) { c -> editarCita(c) }
                adaptador.dataSetFull = citas // Actualiza la lista completa en el adaptador
                lista_citas.adapter = adaptador
                //false para que se muestre en orden inverso
                //definir como quiero q se meuestren
                lista_citas.layoutManager= LinearLayoutManager(
                    this, LinearLayoutManager.VERTICAL,false)
                //añadirlo a cyrcleview
                lista_citas.adapter=adaptador
            }

    }// FIN muestraCitas()

    //redirige al activity
    private fun editarCita(c: CitaPaciente){
        val intent = Intent(this, EditarCitaActivity::class.java)
        //pasar info String de una activity a otra
        //intent.putExtra("especialidad", c.especialidad)
        //intent.putExtra("id", c.id)

        //pasar objetos de activities -> tienen que ser serialisable
        intent.putExtra("cita",c)
        startActivity(intent)
    }

    //los datos que queremos sacar en la tabla (parámetro)
    //el adaptador recibe los datos
    //se le definen dos atributos a la clase, una lista y una funcion que recibe una cita y no devuelve nada
    inner class AdaptadorCitas(private var dataSet: List<CitaPaciente>,
                               private val clickCita: (CitaPaciente)-> Unit) :
        RecyclerView.Adapter<AdaptadorCitas.CitaViewHolder>() {

        var dataSetFull: List<CitaPaciente> = dataSet.toList()

        fun filter(text: String) {
            val filteredCitas: List<CitaPaciente> = if (text.isEmpty()) {
                dataSetFull // Usa la lista completa si no hay texto de búsqueda
            } else {
                dataSetFull.filter { // Filtra la lista completa
                    it.consulta.contains(text, ignoreCase = true) ||
                            it.hospital.contains(text, ignoreCase = true) ||
                            it.medico.contains(text, ignoreCase = true) ||
                            it.fecha.toString().contains(text, ignoreCase = true)
                }
            }
            dataSet = filteredCitas // Actualiza dataSet con los resultados filtrados
            notifyDataSetChanged() // Notifica al adaptador del cambio
        }
        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        //define el formato de las filas
        inner class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            //campos de las filas de las tablas
            val consulta: TextView
            val hospital: TextView
            val medico: TextView
            val fechaCita: TextView
            val horaCita: TextView
            init {
                // constructor de la clase interna
                consulta = view.findViewById(R.id.consulta)
                fechaCita = view.findViewById(R.id.fechaCita)
                horaCita = view.findViewById(R.id.horaCita)
                hospital = view.findViewById(R.id.hospital)
                medico = view.findViewById(R.id.medico)
            }
        }

        // Create new views (invoked by the layout manager)
        //clase adaptador
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CitaViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_cita_paciente, viewGroup, false)
            return CitaViewHolder(view)
        }
        // Replace the contents of a view (invoked by the layout manager)
        //parametro posicion, posicion de cada una de las filas de la tabla
        //método que muestra la informacion en cada una de las listas de la tabla
        override fun onBindViewHolder(viewHolder: CitaViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.consulta.text = "Consulta ${dataSet[position].consulta}"
            viewHolder.hospital.text = dataSet[position].hospital
            viewHolder.medico.text = dataSet[position].medico

            // Formato de fecha
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            viewHolder.fechaCita.text = dateFormat.format(dataSet[position].fecha.time)

            // Formato de hora
            val timeFormat = SimpleDateFormat("HH:mm")
            viewHolder.horaCita.text = timeFormat.format(dataSet[position].fecha.time)
            //itemView es la fila de la tabla
            //fucnionalidad para poder editar citas
            //datasetCita -> es la posicion en la lista de citas -> osea una cita
            viewHolder.itemView.setOnClickListener{(clickCita(dataSet[position]))}
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }

}