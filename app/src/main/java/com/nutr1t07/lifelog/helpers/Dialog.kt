package com.nutr1t07.lifelog.helpers

import android.app.Activity
import android.app.TimePickerDialog
import android.view.View
import android.view.Window
import android.widget.TextView
import com.nutr1t07.lifelog.R
import kotlinx.android.synthetic.main.dialog_title.view.*
import java.text.SimpleDateFormat
import java.util.*


fun Activity.setupDialog(
    view: View,
    dialog: androidx.appcompat.app.AlertDialog,
    titleId: Int = 0,
    titleText: String = "",
    callback: (() -> Unit)? = null
) {
    var titleView: TextView? = null
    if (titleId != 0 || titleText.isNotEmpty()) {
        titleView = layoutInflater.inflate(R.layout.dialog_title, null) as TextView
        titleView.dialog_title_textview.apply {
            if (titleText.isNotEmpty()) {
                text = titleText
            } else {
                setText(titleId)
            }
        }
    }

    dialog.apply {
        setView(view)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCustomTitle(titleView)
        setCanceledOnTouchOutside(true)
        show()
    }

    callback?.invoke()
}

fun Activity.showTimePicker(callback: (str: String) -> Unit) {
    val c: Calendar = Calendar.getInstance()
    val hour = c.get(Calendar.AM_PM)
    val min = c.get(Calendar.MINUTE)
    val year = c.get(Calendar.YEAR)
    val mon = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)

    val dateFormat = SimpleDateFormat("HH:mm", Locale.US)

    val mTimePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, sHour, sMin ->
        val date = Date(year, mon, day, sHour, sMin)
        callback.invoke(dateFormat.format(date))
    }, hour, min, true)

    mTimePicker.show()
}