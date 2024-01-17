package com.us.app_gsti.pantallas.modelo

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.util.Calendar

//objeto de tipo calendario

//clase que nos va a permitir abrir el calendario y que se peuda escoger
class SeleccionHora (val listener: (hour: Int, minute: Int) -> Unit) :
    DialogFragment(), TimePickerDialog.OnTimeSetListener {
    //funcion que se va a invocar cuando el user elija una fecha
    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        listener(hour, minute)
    }


    //metodo para que aparezca en la pantalla un calendario con la fecha de ese dia por defecto
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance() //crea con el día de hoy por defecto
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)
        val picker = TimePickerDialog(activity as Context, this, hour, minute, true)
        return picker
    }
}