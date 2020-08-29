package com.nutr1t07.lifelog.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutr1t07.lifelog.App
import com.nutr1t07.lifelog.EventStatListViewAdapter
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.*
import kotlinx.android.synthetic.main.activity_stat_sector.*
import java.util.*

class StatSectorActivity : AppCompatActivity() {
    private lateinit var eventStatSectAdapter: EventStatListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stat_sector)

        val events: TreeMap<Date, MutableList<Event>> =
            (applicationContext as App).dataManager.events
        eventStatSectAdapter = EventStatListViewAdapter(
            EventDataManager(
                this,
                applicationContext,
                listOf()
            ), events2StatListItems(events)
        )

        val viewManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        event_sector_stat_recyclerView.apply {
            layoutManager = viewManager
            adapter = eventStatSectAdapter
        }
    }


    private fun events2StatListItems(events: TreeMap<Date, MutableList<Event>>): MutableList<ListItem> {
        val items: MutableList<ListItem> = mutableListOf()
        for (date in events.keys) {
            items.add(StatSectorEventItem(events[date]!!))
            val header = DateItem(date, items.size)
            items.add(header)
        }
        return items
    }
}