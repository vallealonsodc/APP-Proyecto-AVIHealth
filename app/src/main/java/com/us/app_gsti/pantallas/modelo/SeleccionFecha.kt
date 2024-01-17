package com.us.app_gsti.pantallas.modelo

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

//objeto de tipo calendario

//clase que nos va a permitir abrir el calendario y que se peuda escoger
class SeleccionFecha (val listener: (day: Int, month: Int, year: Int) -> Unit) :
    DialogFragment(), DatePickerDialog.OnDateSetListener {
    //funcion que se va a invocar cuando el user elija una fecha
    override fun onDateSet(view: DatePicker?, año: Int, mes: Int, dia: Int) {
        listener(dia,mes,año)
    }


    //metodo para que aparezca en la pantalla un calendario con la fecha de ese dia por defecto
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance() //crea con el día de hoy por defecto
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val picker = DatePickerDialog(activity as Context, this, year, month, day)
        return picker
    }
}