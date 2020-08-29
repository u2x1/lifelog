package com.nutr1t07.lifelog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nutr1t07.lifelog.data.DateItem
import com.nutr1t07.lifelog.data.EventDataManager
import com.nutr1t07.lifelog.data.ListItem
import com.nutr1t07.lifelog.data.StatSectorEventItem
import com.nutr1t07.lifelog.helpers.getyyyyMMddFromDate
import com.nutr1t07.lifelog.view.EventCircle

class EventStatListViewAdapter(val parent: EventDataManager, var viewItems: MutableList<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = parent.context
    val activity = parent.activity

    class EventStatSectViewHolder(
        private val parent: EventStatListViewAdapter,
        v: View
    ) : RecyclerView.ViewHolder(v) {
        val listCircle = v.findViewById(R.id.item_stat_event_circle) as EventCircle

    }

    override fun getItemViewType(position: Int): Int = viewItems[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
        return if (viewType == ListItem.TYPE_EVENT_STAT_SECTOR)
            EventStatSectViewHolder(this, view)
        else
            EventListViewAdapter.DateViewHolder(view)
    }

    override fun getItemCount(): Int = viewItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = viewItems[position]) {
            is DateItem -> {
                if (holder is EventListViewAdapter.DateViewHolder) {
                    val date = item.date
                    holder.listDate.text = getyyyyMMddFromDate(date)
                }
            }

            is StatSectorEventItem -> {
                if (holder is EventStatSectViewHolder) {
                    holder.listCircle.events = item.events
                }
            }
        }
    }

}