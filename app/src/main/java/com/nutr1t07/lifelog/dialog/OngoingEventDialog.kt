package com.nutr1t07.lifelog.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.SharedPreferences
import android.view.View
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.Event
import com.nutr1t07.lifelog.dialog.AddEventDialog.Companion.EVENT_ONGOING_FROM_TIME
import com.nutr1t07.lifelog.dialog.AddEventDialog.Companion.EVENT_ONGOING_NAME
import com.nutr1t07.lifelog.dialog.AddEventDialog.Companion.EVENT_ONGOING_NAME_SEC
import com.nutr1t07.lifelog.dialog.AddEventDialog.Companion.EVENT_ONGOING_TYPE
import com.nutr1t07.lifelog.dialog.AddEventDialog.Companion.HAS_EVENT_ONGOING
import com.nutr1t07.lifelog.helpers.*
import kotlinx.android.synthetic.main.dialog_ongoing_event.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class OngoingEventDialog(
    private val activity: Activity,
    context: Context,
    types: List<String>,
    callback: (event: Event?) -> Unit
) {
    private var relativeTimeFlag: Boolean = true

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_ongoing_event, null)

        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putBoolean(HAS_EVENT_ONGOING, false).apply()

        val startTimestamp = sharedPreferences.getLong(EVENT_ONGOING_FROM_TIME, -1)
        val eventName = sharedPreferences.getString(EVENT_ONGOING_NAME, null)
        val eventNameSec = sharedPreferences.getString(EVENT_ONGOING_NAME_SEC, null)
        val eventType = sharedPreferences.getInt(EVENT_ONGOING_TYPE, -1)

        if (startTimestamp == -1L || eventName == null || eventType == -1) {
            AlertDialog.Builder(activity)
                .setTitle(R.string.get_event_from_preference_failed)
                .setPositiveButton(R.string.ok, null)
                .create().show()
        } else {
            val startTime = Date(startTimestamp)

            initView(view)
            view.findViewById<TextView>(R.id.ongoing_event_details).text =
                context.resources.getString(
                    R.string.event_detail,
                    eventName,
                    eventNameSec ?: "",
                    getyyyyMMddHHmmFromDate(startTime),
                    types[eventType]
                )

            MaterialAlertDialogBuilder(activity)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.cancel, null)
                .setOnCancelListener { callback(null) }
                .create().apply {
                    activity.setupDialog(view, this, R.string.an_ongoing_event) {
                        getButton(BUTTON_NEGATIVE).setOnClickListener { cancel() }
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            var endTime: Date? = null
                            if (relativeTimeFlag) {
                                when {
                                    ongoing_relative_time_editText.text.isEmpty() ->
                                        Toast.makeText(
                                            activity,
                                            R.string.time_empty,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    else -> {
                                        val timestamp: Long?
                                        try {
                                            val value =
                                                ongoing_relative_time_editText.text.toString()
                                                    .toLong()
                                            timestamp =
                                                if (ongoing_relative_time_spinner.selectedItemPosition == 0)
                                                    value * TickInSecond * 60L
                                                else
                                                    value * TickInSecond * 60L * 60L
                                        } catch (e: ParseException) {
                                            Toast.makeText(
                                                activity,
                                                R.string.time_format_error, Toast.LENGTH_SHORT
                                            ).show()
                                            return@setOnClickListener
                                        }
                                        endTime = Date(Date().time - timestamp)
                                    }
                                }
                            } else {
                                when {
                                    ongoing_precise_time_editText.text.isEmpty() ->
                                        Toast.makeText(
                                            activity,
                                            R.string.time_empty,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    else -> {
                                        val endTimeNullable =
                                            getDateFromHHmm(ongoing_precise_time_editText.text.toString())
                                        if (endTimeNullable == null) {
                                            Toast.makeText(
                                                activity,
                                                R.string.time_format_error, Toast.LENGTH_SHORT
                                            ).show()
                                            return@setOnClickListener
                                        }
                                        endTime = endTimeNullable
                                    }
                                }
                            }

                            if (endTime == null) {
                                return@setOnClickListener
                            } else if (startTimestamp > endTime.time) {
                                Toast.makeText(
                                    activity,
                                    R.string.end_time_earlier_than_start_time, Toast.LENGTH_SHORT
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

    private fun initView(view: View) {
        val spinner: Spinner = view.findViewById(R.id.ongoing_relative_time_spinner)
        val relativeTimeEditText: TextView = view.findViewById(R.id.ongoing_relative_time_editText)
        val preciseTimeEditText: TextView = view.findViewById(R.id.ongoing_precise_time_editText)
        val preciseTimeBtn: Button = view.findViewById(R.id.ongoing_event_precise_time_btn)

        val setRelateEnable = { x: Boolean ->
            relativeTimeEditText.isEnabled = x
            spinner.isEnabled = x
            preciseTimeEditText.isEnabled = !x
            preciseTimeBtn.isEnabled = !x
        }

        relativeTimeEditText.doOnTextChanged { text, _, _, _ ->
            val time = text.toString().toIntOrNull()
            if (time != null) {
                val timestamp =
                    if (spinner.selectedItemPosition == 0) time * TickInSecond * 60L else time * TickInSecond * 60L * 60L
                preciseTimeEditText.text =
                    SimpleDateFormat("HH:mm", Locale.US).format(Date(Date().time - timestamp))
            }
        }
        relativeTimeEditText.text = "0"

        view.findViewById<RadioButton>(R.id.ongoing_relative_time_radio).setOnClickListener {
            relativeTimeFlag = true
            setRelateEnable(true)
        }

        view.findViewById<RadioButton>(R.id.ongoing_precise_time_radio).setOnClickListener {
            relativeTimeFlag = false
            setRelateEnable(false)
        }

        view.findViewById<Button>(R.id.ongoing_event_precise_time_btn).setOnClickListener {
            activity.showTimePicker {
                view.findViewById<TextView>(R.id.ongoing_precise_time_editText).text = it
            }
        }
    }
}