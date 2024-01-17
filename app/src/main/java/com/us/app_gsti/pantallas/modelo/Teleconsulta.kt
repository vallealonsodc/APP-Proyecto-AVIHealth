package com.us.app_gsti.pantallas.modelo

import java.io.Serializable

class Teleconsulta(
    val id: String,
    val medico: String,
    val comentario: String,
    //val imagen: Bitmap
    val imagenUrl: String, // Campo agregado para la URL de la imagen
    val respuesta: String,
    var isExpanded: Boolean = false // Estado de visibilidad agregado
): Serializable {
}
