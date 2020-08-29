package com.nutr1t07.lifelog

import android.app.Application
import com.nutr1t07.lifelog.data.EventDataManager

class App : Application() {
    lateinit var dataManager: EventDataManager
}