package com.nutr1t07.lifelog.data

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.nutr1t07.lifelog.EventListViewAdapter
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.helpers.contextEventDB
import com.nutr1t07.lifelog.helpers.modyyyyMMddFromDate
import java.util.*

abstract class ListItem(val type: Int) {
    companion object {
        const val TYPE_HEADER =
            R.layout.recyclerview_item_date
        const val TYPE_EVENT =
            R.layout.recyclerview_item_event
        const val TYPE_EVENT_STAT_SECTOR =
            R.layout.recyclerview_item_event_sector_stat
    }
}

class DateItem(val date: Date, val position: Int) : ListItem(
    TYPE_HEADER
)

class EventItem(var event: Event) : ListItem(
    TYPE_EVENT
)

class StatSectorEventItem(var events: MutableList<Event>) : ListItem(
    TYPE_EVENT_STAT_SECTOR
)

fun getEventColor(type: Int) =
    when (type) {
        0 -> R.color.colorEventBlue
        1 -> R.color.colorEventOrange
        2 -> R.color.colorEventGreen
        3 -> R.color.colorEventPink
        else -> R.color.colorEventLightBlack
    }

class EventDataManager(
    val activity: Activity,
    val context: Context,
    types: List<String>
) {
    interface EventDataChangeListener {
        fun onDataChange()
    }

    var listener: EventDataChangeListener? = null

    fun setOnDataChangeListener(listener: EventDataChangeListener) {
        this.listener = listener
    }

    var eventViewAdapter: EventListViewAdapter =
        EventListViewAdapter(this, mutableListOf(), types)

    lateinit var events: TreeMap<Date, MutableList<Event>>

    private fun events2EventListItems(events: TreeMap<Date, MutableList<Event>>): MutableList<ListItem> {
        val items: MutableList<ListItem> = mutableListOf()
        for (date in events.keys) {
            for (event in events[date]!!)
                items.add(EventItem(event))
            val header = DateItem(date, items.size)
            items.add(header)
        }
        return items
    }


    fun getEvents(callback: ((TreeMap<Date, MutableList<Event>>) -> Unit)) {
        Thread {
            val eventsGroupedByDate: TreeMap<Date, MutableList<Event>> = TreeMap()
            for (e in context.contextEventDB.getEvents()) {
                val eTime = modyyyyMMddFromDate(e.startTime)
                if (eventsGroupedByDate[eTime] == null)
                    eventsGroupedByDate[eTime] = mutableListOf()
                eventsGroupedByDate[eTime]!!.add(e)
            }

            Handler(Looper.getMainLooper()).post {
                callback(eventsGroupedByDate)
            }
        }.start()
    }

    fun getEventsModified(since: Date, callback: ((MutableList<Event>) -> Unit)) {
        Thread {
            val events: MutableList<Event> = context.contextEventDB.getEventModified(since)

            Handler(Looper.getMainLooper()).post {
                callback(events)
            }
        }.start()
    }

    fun insertEvent(newEvent: Event) {
        Thread {
            val id = context.contextEventDB.insertOrUpdateEvent(newEvent)
            val startTime = modyyyyMMddFromDate(newEvent.startTime)
            if (events[startTime] == null)
                events[startTime] = mutableListOf()
            val event = Event(
                id,
                newEvent.startTime,
                newEvent.endTime,
                newEvent.modifyTime,
                newEvent.name,
                newEvent.nameSec,
                newEvent.type
            )
            events[startTime]!!.add(event)
            listener?.onDataChange()

            val dateItem =
                eventViewAdapter.viewItems.find { it is DateItem && modyyyyMMddFromDate(it.date) == startTime } as? DateItem?

            if (dateItem != null) {
                val pos = dateItem.position
                eventViewAdapter.viewItems[pos] =
                    DateItem(dateItem.date, pos + 1)
                eventViewAdapter.viewItems.add(
                    pos,
                    EventItem(event)
                )
                Handler(Looper.getMainLooper()).post {
                    eventViewAdapter.notifyItemChanged(pos)
                }
            } else {
                val dateItemIndex =
                    eventViewAdapter.viewItems.indexOfLast { it is DateItem && it.date < startTime } + 1
                eventViewAdapter.viewItems.add(
                    dateItemIndex,
                    DateItem(startTime, dateItemIndex + 1)
                )
                eventViewAdapter.viewItems.add(
                    dateItemIndex,
                    EventItem(event)
                )

                Handler(Looper.getMainLooper()).post {
                    eventViewAdapter.notifyItemRangeInserted(dateItemIndex, 2)
                }
            }
        }.start()
    }

    fun updateEvent(newEvent: Event) {
        Thread {
            context.contextEventDB.insertOrUpdateEvent(newEvent)
            listener?.onDataChange()
            val idAtOriginEvents =
                eventViewAdapter.viewItems.indexOfFirst { it is EventItem && it.event.id == newEvent.id }
            if (idAtOriginEvents != -1) {
                eventViewAdapter.viewItems[idAtOriginEvents] =
                    EventItem(newEvent)
                Handler(Looper.getMainLooper()).post {
                    eventViewAdapter.notifyItemChanged(idAtOriginEvents)
                }
            }
        }.start()
    }

    fun refreshEvents(newEvents: MutableList<Event>) {
        Thread {
            for (event in newEvents) {
                context.contextEventDB.insertOrUpdateEvent(event)
            }

            val eventsGroupedByDate: TreeMap<Date, MutableList<Event>> = TreeMap()
            for (e in context.contextEventDB.getEvents()) {
                val eTime = modyyyyMMddFromDate(e.startTime)
                if (eventsGroupedByDate[eTime] == null)
                    eventsGroupedByDate[eTime] = mutableListOf()
                eventsGroupedByDate[eTime]!!.add(e)
            }
            events = eventsGroupedByDate
            eventViewAdapter.viewItems = events2EventListItems(events)
            listener?.onDataChange()
            Handler(Looper.getMainLooper()).post {
                eventViewAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    fun refreshEvents(newEvents: TreeMap<Date, MutableList<Event>>) {
        events = newEvents
        eventViewAdapter.viewItems = events2EventListItems(events)
        listener?.onDataChange()
        Handler(Looper.getMainLooper()).post {
            eventViewAdapter.notifyDataSetChanged()
        }
    }

    fun removeEvent(event: Event) {
        Thread {
            context.contextEventDB.deleteEvent(event)
            val idAtOriginEvents =
                eventViewAdapter.viewItems.indexOfFirst { it is EventItem && it.event.id == event.id }
            var dateDeleted = false
            // If the item being removed is the only item with that date, remove the date as well
            if (eventViewAdapter.viewItems[idAtOriginEvents + 1] is DateItem &&
                eventViewAdapter.viewItems.size < 3 ||
                eventViewAdapter.viewItems[idAtOriginEvents - 1] is DateItem
            ) {
                eventViewAdapter.viewItems.removeAt(idAtOriginEvents + 1)
                dateDeleted = true
            }
            eventViewAdapter.viewItems.removeAt(idAtOriginEvents)
            listener?.onDataChange()
            Handler(Looper.getMainLooper()).post {
                if (dateDeleted) eventViewAdapter.notifyItemRemoved(idAtOriginEvents + 1)
                eventViewAdapter.notifyItemRemoved(idAtOriginEvents)
            }
        }.start()
    }
}