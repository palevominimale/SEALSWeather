package my.weather.ui.main

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import android.widget.RemoteViews
import my.weather.R
import my.weather.data.ForecastRepository
import my.weather.interaction.IntentHelpers
import my.weather.logic.APPWIDGET_UPDATE
import my.weather.logic.REFRESHED
import my.weather.logic.SELF_REFRESH
import java.time.LocalDateTime

class WeatherWidget : AppWidgetProvider() {

    private lateinit var fR : ForecastRepository
    private lateinit var iH : IntentHelpers

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        fR = ForecastRepository(context)
        iH = IntentHelpers(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
            ComponentName(context, context.javaClass)
        )
        when(intent.action) {
            REFRESHED -> {
                Log.println(Log.DEBUG, "WIDGET", "Received intent: $intent")
                val i = Intent(APPWIDGET_UPDATE)
                PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE).send()
            }
            APPWIDGET_UPDATE -> {
                Log.println(Log.DEBUG, "WIDGET UPDATE", "Received intent: $intent")

                Log.println(Log.DEBUG, "Updating", appWidgetIds.toString())
                for (appWidgetId in appWidgetIds) {
                    Log.println(Log.DEBUG, "Updating", appWidgetId.toString())
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                }
            }
            SELF_REFRESH -> {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                    Log.println(Log.DEBUG, "WIDGET", "Self-refresh intent: $intent")
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int ){
        val now = LocalDateTime.now().hour
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        val intent = Intent()
        intent.action = SELF_REFRESH
        val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.tempWidget, pi)
        fR.getById(now).run {
            views.setTextViewText(R.id.humidityWidget, "RH: ${humidity?.toInt()}%")
            views.setTextViewText(R.id.pressureWidget, "$pressure hPa")
            views.setTextViewText(R.id.tempWidget, "$temp°C")
            views.setTextViewText(R.id.widgetWeatherType, "$weatherDescription")
            views.setImageViewResource(R.id.widgetWeatherIcon, weatherIcon ?: R.drawable.wi_meteor)
            views.setImageViewResource(R.id.widgetWindSpd, windIcon ?: R.drawable.wi_meteor)
            val bmpOriginal: Bitmap = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.winddir)
            val bmpResult = Bitmap.createBitmap(bmpOriginal.width, bmpOriginal.height, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(bmpResult)
            tempCanvas.rotate(windIconRotation ?: 0.0F, bmpOriginal.width / 2.0F, bmpOriginal.height / 2.0F)
            tempCanvas.drawBitmap(bmpOriginal, 0F, 0F, null)
            views.setImageViewBitmap(R.id.widgetWindDir, bmpResult)
        }
        fR.getById(now+1).run {
            views.setTextViewText(R.id.wlTemp, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+2).run {
            views.setTextViewText(R.id.wlTemp1, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon1, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+3).run {
            views.setTextViewText(R.id.wlTemp2, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon2, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+4).run {
            views.setTextViewText(R.id.wlTemp3, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon3, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+5).run {
            views.setTextViewText(R.id.wlTemp4, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon4, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+6).run {
            views.setTextViewText(R.id.wlTemp5, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon5, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+7).run {
            views.setTextViewText(R.id.wlTemp6, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon6, weatherIcon ?: R.drawable.wi_meteor)
        }
        fR.getById(now+8).run {
            views.setTextViewText(R.id.wlTemp7, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon7, weatherIcon ?: R.drawable.wi_meteor)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.println(Log.DEBUG, "WIDGET UPDATE", "Widget was updated!")
    }
}

