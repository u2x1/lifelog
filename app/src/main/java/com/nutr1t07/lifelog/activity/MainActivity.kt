package com.nutr1t07.lifelog.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nutr1t07.lifelog.App
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.data.EventDataManager
import com.nutr1t07.lifelog.data.syncEvents
import com.nutr1t07.lifelog.dialog.AddEventDialog
import com.nutr1t07.lifelog.dialog.AddEventDialog.Companion.HAS_EVENT_ONGOING
import com.nutr1t07.lifelog.dialog.OngoingEventDialog
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {
    private val typeList = mutableListOf<String>()
    private lateinit var dataManager: EventDataManager
    private var hasOngoing: Boolean = false
    private var lastSrollY: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val types = resources.getStringArray(R.array.event_type_array)

        for (type: String in types)
            typeList += type

        (applicationContext as App).dataManager = EventDataManager(
            this,
            applicationContext,
            typeList
        )

        dataManager = (applicationContext as App).dataManager
        dataManager.getEvents { dataManager.refreshEvents(it) }

        val viewManager = LinearLayoutManager(this)
        viewManager.reverseLayout = true
        viewManager.stackFromEnd = true
        lists_recyclerView.apply {
            layoutManager = viewManager
            adapter = dataManager.eventViewAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val currentY = (this@apply as RecyclerView).computeVerticalScrollOffset()
                        if (lastSrollY > currentY) {
                            if (!supportActionBar!!.isShowing)
                                supportActionBar!!.show()
                            if (!new_event_fab.isShown)
                                new_event_fab.show()
                            if (hasOngoing && !ongoing_fab.isShown)
                                ongoing_fab.show()
                        } else {
                            if (supportActionBar!!.isShowing)
                                supportActionBar!!.hide()
                            if (new_event_fab.isShown)
                                new_event_fab.hide()
                            if (ongoing_fab.isShown)
                                ongoing_fab.hide()
                        }
                        lastSrollY = currentY
                    }
                }
            })
        }


        findViewById<FloatingActionButton>(R.id.new_event_fab).setOnClickListener { showAddEventDialog() }
        findViewById<FloatingActionButton>(R.id.ongoing_fab).setOnClickListener { showEventOngoingDialog() }

        val activityPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        val syncFlag: Boolean = activityPreferenceManager.getBoolean("sync", false)
        if (syncFlag)
            syncEvents(applicationContext, null)


        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (sharedPreference.getBoolean(HAS_EVENT_ONGOING, false)) {
            hasOngoing = true
            showEventOngoingDialog()
        } else {
            showAddEventDialog()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_stat_sect -> {
                val intent = Intent(this, StatSectorActivity::class.java)
                intent.putExtra("events", dataManager.events)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddEventDialog() {
        AddEventDialog(this, applicationContext, typeList) {
            if (it != null)
                dataManager.insertEvent(it)
            else {
                hasOngoing = true
                ongoing_fab.show()
            }

        }
    }

    private fun showEventOngoingDialog() {
        OngoingEventDialog(this, applicationContext, typeList) {
            if (it != null) {
                dataManager.insertEvent(it)
                ongoing_fab.hide()
                hasOngoing = false
            } else {
                if (!ongoing_fab.isShown) {
                    ongoing_fab.show()
                }
            }

        }
    }
}