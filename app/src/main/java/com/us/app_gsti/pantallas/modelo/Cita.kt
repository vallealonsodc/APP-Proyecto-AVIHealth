package com.us.app_gsti.pantallas.modelo

import java.io.Serializable
import java.util.Calendar

//definir clase para definir objetos -> con esos atributos
//serializable hace posible que el tipo de objeto cita se pueda pasar de un intent a otro
class Cita (val id: String, val consulta: String, val fecha: Calendar, val paciente: String,
            val hospital: String): Serializable{

    //cuando hagamos click en una cita podamos acceder a la cita para modificarla o borrarla

}