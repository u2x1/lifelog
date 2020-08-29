package com.nutr1t07.lifelog.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.Event
import com.nutr1t07.lifelog.helpers.getDateFromyyyyMMddHHmm
import com.nutr1t07.lifelog.helpers.getyyyyMMddHHmmFromDate
import com.nutr1t07.lifelog.helpers.setupDialog
import kotlinx.android.synthetic.main.dialog_edit_event.*
import java.util.*

class EditEventDialog(
    private val activity: Activity,
    val context: Context,
    private val types: List<String>,
    private val originEvent: Event,
    callback: (state: Int, newEvent: Event?) -> Unit
) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_edit_event, null)

        initView(originEvent, view)

        MaterialAlertDialogBuilder(activity)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialog(view, this, R.string.edit_an_event) {
                    edit_event_delete_event_btn.setOnClickListener {
                        AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.delete_event, originEvent.name))
                            .setPositiveButton(R.string.confirm) { dialog, _ ->
                                dialog.dismiss()
                                this.dismiss()
                                callback(STATE_REMOVED, null)
                            }
                            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                            .create().show()
                    }
                    getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        when {
                            edit_event_name_editText.text.isEmpty() ->
                                Toast.makeText(
                                    activity,
                                    R.string.event_name_empty,
                                    Toast.LENGTH_SHORT
                                ).show()

                            (edit_event_types_group.checkedRadioButtonId == -1) ->
                                Toast.makeText(
                                    activity,
                                    R.string.event_type_empty,
                                    Toast.LENGTH_SHORT
                                ).show()

                            (edit_event_start_time_editText.text.isEmpty()
                                    || edit_event_end_time_editText.text.isEmpty()) ->
                                Toast.makeText(
                                    activity,
                                    R.string.time_empty,
                                    Toast.LENGTH_SHORT
                                ).show()

                            else -> {
                                val eventName: String = edit_event_name_editText.text.toString()
                                var eventNameSec: String? =
                                    edit_event_name_sec_editText.text.toString()
                                eventNameSec = if (eventNameSec!!.isEmpty()) null else eventNameSec
                                val selectedType: Int = with(edit_event_types_group) {
                                    indexOfChild(findViewById<View>(checkedRadioButtonId))
                                }
                                val startTime: Date? =
                                    getDateFromyyyyMMddHHmm(edit_event_start_time_editText.text.toString())
                                val endTime: Date? =
                                    getDateFromyyyyMMddHHmm(edit_event_end_time_editText.text.toString())

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
                                    STATE_EDITED,
                                    Event(
                                        originEvent.id,
                                        startTime,
                                        endTime,
                                        Date(),
                                        eventName,
                                        eventNameSec,
                                        selectedType
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }

    private fun initView(originEvent: Event, view: View) {
        val nameEditText: EditText = view.findViewById(R.id.edit_event_name_editText)
        val nameSecEditText: EditText = view.findViewById(R.id.edit_event_name_sec_editText)
        val startTimeEditText: EditText = view.findViewById(R.id.edit_event_start_time_editText)
        val endTimeEditText: EditText = view.findViewById(R.id.edit_event_end_time_editText)


        val typesGroupView: RadioGroup = view.findViewById(R.id.edit_event_types_group)

        for (type in types) {
            val radioButton = RadioButton(activity)
            radioButton.text = type
            typesGroupView.addView(radioButton)
        }

        startTimeEditText.setText(getyyyyMMddHHmmFromDate(originEvent.startTime))
        endTimeEditText.setText(getyyyyMMddHHmmFromDate(originEvent.endTime))
        nameEditText.setText(originEvent.name)
        nameSecEditText.setText(originEvent.nameSec ?: "")

        typesGroupView.getChildAt(originEvent.type)?.id?.let {
            typesGroupView.check(it)
        }
    }

    companion object {
        const val STATE_EDITED = 0
        const val STATE_REMOVED = 1
    }
}