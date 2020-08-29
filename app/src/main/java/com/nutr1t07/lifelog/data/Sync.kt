package com.nutr1t07.lifelog.data

import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.nutr1t07.lifelog.App
import com.nutr1t07.lifelog.R
import com.nutr1t07.lifelog.helpers.lastSyncTime
import com.nutr1t07.lifelog.helpers.userSession
import com.nutr1t07.lifelog.utils.event2JsonObj
import com.nutr1t07.lifelog.utils.jsonObj2Event
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

const val mHost = "https://privacy.com"

fun syncEvents(context: Context, callback: (() -> Unit)?) {
    val queue: RequestQueue = Volley.newRequestQueue(context)
    downloadEvents(queue, context) {
        val dataManager = (context as App).dataManager
        val since = Date(context.lastSyncTime)
        dataManager.getEventsModified(since) {
            uploadEvents(queue, context, it, callback)
        }
    }
}

private fun downloadEvents(queue: RequestQueue, context: Context, callback: (() -> Unit)?) {
    val url = "$mHost/event/fetch/"
    val json = JSONObject()
    json.put("userSession", context.userSession)
    json.put("lastSyncTime", context.lastSyncTime)
    queue.add(
        JsonObjectRequest(Request.Method.POST, url, json,
            Response.Listener {
                when (it.getInt("code")) {
                    111 -> {
                        Toast.makeText(
                            context,
                            R.string.user_session_not_exist,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    110 -> {
                        val dataManager: EventDataManager =
                            (context as App).dataManager
                        val eventJsonArray: JSONArray =
                            it.getJSONObject("values").getJSONArray("contents")
                        val eventArray: MutableList<Event> = mutableListOf()
                        for (i in 0 until eventJsonArray.length()) {
                            eventArray.add(jsonObj2Event(eventJsonArray[i] as JSONObject))
                        }
                        dataManager.refreshEvents(eventArray)
                        callback?.invoke()
                    }
                }
            }
            , Response.ErrorListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.download_events_failed, it.toString()),
                    Toast.LENGTH_LONG
                ).show()
            })
    )
}


private fun uploadEvents(
    queue: RequestQueue,
    context: Context,
    events: MutableList<Event>,
    callback: (() -> Unit)?
) {
    val url = "$mHost/event/upload/"
    val eventJsonArray = JSONArray()
    for (event in events) {
        eventJsonArray.put(event2JsonObj(event))
    }
    val json = JSONObject()
    json.put("userSession", context.userSession)
    json.put("events", eventJsonArray)
    queue.add(
        JsonObjectRequest(Request.Method.POST, url, json,
            Response.Listener {
                when (it.getInt("code")) {
                    111 -> {
                        Toast.makeText(
                            context,
                            R.string.user_session_not_exist,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    110 -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.sync_success),
                            Toast.LENGTH_LONG
                        ).show()
                        context.lastSyncTime = Date().time
                        callback?.invoke()
                    }
                }
            }
            , Response.ErrorListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.download_events_failed, it.toString()),
                    Toast.LENGTH_LONG
                ).show()
            })
    )
}