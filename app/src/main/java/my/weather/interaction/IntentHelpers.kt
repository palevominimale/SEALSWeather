package my.weather.interaction

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import my.weather.logic.*

class IntentHelpers(val context: Context) {

    fun notifyReady() {
        val intent = Intent(READY)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).send()
    }

    fun notifyReadyAfterRefreshing() {
        val intent = Intent(REFRESHED)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).send()
//        Log.println(Log.DEBUG, "IM", "Refreshed! Unblock swipes! $intent")
    }

    fun notifyIsRefreshing() {
        val intent = Intent(REFRESHING)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).send()
//        Log.println(Log.DEBUG, "IM", "Refreshing! Block swipes! $intent")
    }

    fun requestNewData() {
        val intent = Intent(NEED_DATA).putExtra("type", 1)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).send()
//        Log.println(Log.DEBUG, "IM", "Data was requested! $intent")
    }

    fun notifyHaveNewData() {
        val intent = Intent(NEW_DATA_AVAILABLE)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).send()
//        Log.println(Log.DEBUG, "IM", "New data available! $intent")
    }
}