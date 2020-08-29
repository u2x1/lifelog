package com.nutr1t07.lifelog.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.SharedPreferences
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.Event
import com.nutr1t07.lifelog.helpers.getDateFromHHmm
import com.nutr1t07.lifelog.helpers.setupDialog
import com.nutr1t07.lifelog.helpers.showTimePicker
import kotlinx.android.synthetic.main.dialog_add_event.*
import java.text.SimpleDateFormat
import java.util.*


class AddEventDialog(
    private val activity: Activity,
    context: Context,
    val types: List<String>,
    callback: (event: Event?) -> Unit
) {

    private var startNowFlag: Boolean = true

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_add_event, null)

        setViewOnClick(view)

        MaterialAlertDialogBuilder(activity)
            .setPositiveButton(R.string.add, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialog(view, this, R.string.add_event) {
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        val eventName: String = new_event_name_editText.text.toString()
                        var eventNameSec: String? = new_event_sec_name_editText.text.toString()
                        eventNameSec = if (eventNameSec!!.isEmpty()) null else eventNameSec
                        val eventType: Int = with(new_event_types_group) {
                            indexOfChild(findViewById<View>(checkedRadioButtonId))
                        }

                        when {
                            eventName.isEmpty() ->
                                Toast.makeText(
                                    activity,
                                    R.string.event_name_empty,
                                    Toast.LENGTH_SHORT
                                ).show()

                            eventType == -1 ->
                                Toast.makeText(
                                    activity,
                                    R.string.event_type_empty,
                                    Toast.LENGTH_SHORT
                                ).show()

                            else -> {
                                if (startNowFlag) {
                                    val sharedPreferences: SharedPreferences =
                                        PreferenceManager.getDefaultSharedPreferences(context)
                                    if (sharedPreferences.getBoolean("event_ongoing", false)) {
                                        AlertDialog.Builder(activity)
                                            .setTitle(R.string.already_ongoing)
                                            .setPositiveButton(R.string.ok, null)
                                            .create().show()
                                    }
                                    sharedPreferences.edit()
                                        .putBoolean(HAS_EVENT_ONGOING, true)
                                        .putLong(EVENT_ONGOING_FROM_TIME, Date().time)
                                        .putString(EVENT_ONGOING_NAME, eventName)
                                        .putString(EVENT_ONGOING_NAME_SEC, eventNameSec)
                                        .putInt(EVENT_ONGOING_TYPE, eventType)
                                        .apply()
                                    callback(null)
                                    dismiss()
                                } else {
                                    val startTime =
                                        getDateFromHHmm(new_event_start_time_editText.text.toString())
                                    val endTime =
                                        getDateFromHHmm(new_event_end_time_editText.text.toString())

                                    if (startTime == null || endTime == null) {
                                        Toast.makeText(
                                            activity,
                                            R.string.time_format_error, Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    } else if (startTime.time > endTime.time) {
                                        Toast.makeText(
                                            activity,
                                            R.string.end_time_earlier_than_start_time,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@setOnClickListener
                                    }
                                    dismiss()
                                    callback(
                                        Event(
                                            Date().time,
                                            startTime,
                                            endTime,
                                            Date(),
                                            eventName,
                                            eventNameSec,
                                            eventType
                                        )
                                    )
                                }
                            }
                        }

                    }
                }
            }
    }


    private fun setViewOnClick(view: View) {
        val startTimeTextview: EditText = view.findViewById(R.id.new_event_start_time_editText)
        val endTimeTextview: EditText = view.findViewById(R.id.new_event_end_time_editText)
        val endTimeBtn: Button = view.findViewById(R.id.new_event_end_time_btn)
        val startTimeBtn: Button = view.findViewById(R.id.new_event_start_time_btn)

        val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        startTimeTextview.setText(nowTime)
        endTimeTextview.setText(nowTime)

        startTimeBtn.setOnClickListener {
            activity.showTimePicker { startTimeTextview.setText(it) }
        }
        endTimeBtn.setOnClickListener {
            activity.showTimePicker { endTimeTextview.setText(it) }
        }

        // Add type radio button to the RadioGroup
        val typesGroupView: RadioGroup = view.findViewById(R.id.new_event_types_group)
        for (type in types) {
            val radioButton = RadioButton(activity)
            radioButton.text = type
            typesGroupView.addView(radioButton)
        }

        val setEnable = { x: Boolean ->
            startTimeTextview.isEnabled = x
            endTimeTextview.isEnabled = x
            startTimeBtn.isEnabled = x
            endTimeBtn.isEnabled = x
        }
        view.findViewById<RadioButton>(R.id.precise_time_radioBtn).setOnClickListener {
            startNowFlag = false
            setEnable(true)
        }
        view.findViewById<RadioButton>(R.id.start_from_now_radioBtn).setOnClickListener {
            startNowFlag = true
            setEnable(false)
        }

    }

    companion object {
        const val HAS_EVENT_ONGOING = "event_ongoing"
        const val EVENT_ONGOING_FROM_TIME = "event_ongoing_from"
        const val EVENT_ONGOING_NAME = "event_ongoing_name"
        const val EVENT_ONGOING_NAME_SEC = "event_ongoing_name_sec"
        const val EVENT_ONGOING_TYPE = "event_ongoing_type"
    }
}

