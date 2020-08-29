package com.nutr1t07.lifelog.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices = [Index(value = ["id"], unique = true)])
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long?,
//    @PrimaryKey() val id: Long,       // id is now the create time...
    val startTime: Date,
    val endTime: Date,
    val modifyTime: Date,
    val name: String,
    val nameSec: String?,
    val type: Int
) : Parcelable {

    constructor(parcel: Parcel) : this(
//        parcel.readLong(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        Date(parcel.readLong()),
        Date(parcel.readLong()),
        Date(parcel.readLong()),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(p0: Parcel?, p1: Int) {
//        p0?.writeLong(id)
        p0?.writeValue(id)
        p0?.writeLong(startTime.time)
        p0?.writeLong(endTime.time)
        p0?.writeLong(modifyTime.time)
        p0?.writeString(name)
        p0?.writeString(nameSec)
        p0?.writeInt(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }
}