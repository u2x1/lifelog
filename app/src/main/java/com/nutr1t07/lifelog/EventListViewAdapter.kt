package com.nutr1t07.lifelog

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nutr1t07.lifelog.data.*
import com.nutr1t07.lifelog.data.ListItem.Companion.TYPE_EVENT
import com.nutr1t07.lifelog.dialog.EditEventDialog
import com.nutr1t07.lifelog.dialog.EditEventDialog.Companion.STATE_EDITED
import com.nutr1t07.lifelog.dialog.EditEventDialog.Companion.STATE_REMOVED
import com.nutr1t07.lifelog.helpers.getyyyyMMddFromDate
import java.text.SimpleDateFormat
import java.util.*

class EventListViewAdapter(
    private val parent: EventDataManager
    , var viewItems: MutableList<ListItem>
    , private val types: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = parent.context
    val activity = parent.activity

    class EventViewHolder(
        private val parent: EventListViewAdapter,
        v: View
    ) : RecyclerView.ViewHolder(v), View.OnLongClickListener {
        val listEventName = v.findViewById(R.id.item_event_name_text) as TextView
        val listEventSecName = v.findViewById(R.id.item_event_sec_name_text) as TextView
        val listEventType = v.findViewById(R.id.item_event_type_text) as TextView
        val listEventDuration = v.findViewById(R.id.item_event_duration_text) as TextView
        val listEventTime = v.findViewById(R.id.item_event_time_text) as TextView
        val listEventTypeColor = v.findViewById(R.id.item_event_type_imageView) as ImageView

        lateinit var event: Event

        init {
            v.setOnLongClickListener(this)
        }

        override fun onLongClick(p0: View?): Boolean {
            EditEventDialog(
                parent.activity,
                parent.context,
                parent.types,
                event
            ) { state, newEvent ->
                if (state == STATE_EDITED)
                    newEvent?.let { parent.parent.updateEvent(newEvent) }
                else if (state == STATE_REMOVED)
                    parent.parent.removeEvent(event)
            }
            return true
        }
    }

    class DateViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val listDate = v.findViewById(R.id.item_header_date_textView) as TextView
    }

    override fun getItemViewType(position: Int): Int = viewItems[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
        return if (viewType == TYPE_EVENT)
            EventViewHolder(this, view)
        else
            DateViewHolder(view)
    }

    override fun getItemCount(): Int = viewItems.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = viewItems[position]) {
            is DateItem -> {
                if (holder is DateViewHolder) {
                    val date = item.date
                    holder.listDate.text = getyyyyMMddFromDate(date)
                }
            }

            is EventItem -> {
                if (holder is EventViewHolder) {
                    val event = item.event
                    holder.event = event

                    val eventStartTime = event.startTime
                    val eventEndTime = event.endTime
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
                    var relTime = (holder.event.endTime.time - holder.event.startTime.time) / 1000
                    val hrs = relTime / 3600
                    relTime %= 3600
                    val min = relTime / 60
                    val durationText: String =
                        (if (hrs > 0) hrs.toString() + " " + context.getString(R.string.hours) + " " else "") +
                                if (min != 0L) min.toString() + context.getString(R.string.mins) else ""

                    holder.listEventDuration.text = durationText
                    if (event.nameSec == null) {
                        holder.listEventSecName.visibility = View.INVISIBLE
                    } else {
                        holder.listEventSecName.visibility = View.VISIBLE
                        holder.listEventSecName.text =
                            context.getString(R.string.event_sec_name, event.nameSec)
                    }
                    holder.listEventName.text = event.name
                    holder.listEventTime.text =
                        dateFormat.format(eventStartTime) + " ~ " + dateFormat.format(eventEndTime)
                    holder.listEventType.text =
                        context.resources.getStringArray(R.array.event_type_array)[event.type]
                            ?: context.getString(R.string.unknown)

                    val eventColor =
                        getEventColor(event.type)
                    val background = holder.listEventTypeColor.background
                    if (background is GradientDrawable) {
                        background.setColor(ContextCompat.getColor(context, eventColor))
                    } else if (background is ShapeDrawable) {
                        background.paint.color = ContextCompat.getColor(context, eventColor)
                    }
                }
            }
        }
    }
}