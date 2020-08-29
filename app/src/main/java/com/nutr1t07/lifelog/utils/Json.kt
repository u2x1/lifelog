package com.nutr1t07.lifelog.utils

import com.nutr1t07.lifelog.data.Event
import org.json.JSONObject
import java.util.*

fun jsonObj2Event(obj: JSONObject): Event =
    Event(
        obj.getLong("id"),
        Date(obj.getLong("startTime")),
        Date(obj.getLong("endTime")),
        Date(obj.getLong("modifyTime")),
        obj.getString("name"),
        if (obj.isNull("nameSec")) null else obj.getString("nameSec"),
        obj.getInt("type")
    )

fun event2JsonObj(event: Event): JSONObject {
    val json = JSONObject()
    json.put("id", event.id)
    json.put("startTime", event.startTime.time)
    json.put("endTime", event.endTime.time)
    json.put("modifyTime", event.modifyTime.time)
    json.put("name", event.name)
    json.put("nameSec", event.nameSec)
    json.put("type", event.type)
    return json
}