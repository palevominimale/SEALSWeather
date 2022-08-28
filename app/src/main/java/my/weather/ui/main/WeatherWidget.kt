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
import android.widget.RemoteViews
import my.weather.R
import my.weather.data.ForecastItem
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
                val i = Intent(APPWIDGET_UPDATE)
                PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE).send()
            }
            APPWIDGET_UPDATE -> {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                }
            }
            SELF_REFRESH -> {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int ){
        val empty = ForecastItem()
        val now = LocalDateTime.now().hour
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        val defaultIcon = R.drawable.wi_meteor
        val intent = Intent(context, WeatherWidget::class.java)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, WeatherWidget::class.java))
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        val pi = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.tempWidget, pi)
        views.setOnClickPendingIntent(R.id.widgetRefresh, pi)

        (fR.getById(now) ?: empty).run {
            val bmpOriginal: Bitmap = BitmapFactory.decodeResource(context.applicationContext.resources, R.drawable.winddir)
            val bmpResult = Bitmap.createBitmap(bmpOriginal.width, bmpOriginal.height, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(bmpResult)
            tempCanvas.rotate(windIconRotation ?: 0.0F, bmpOriginal.width / 2.0F, bmpOriginal.height / 2.0F)
            tempCanvas.drawBitmap(bmpOriginal, 0F, 0F, null)
            views.setTextViewText(R.id.humidityWidget, "RH: ${(humidity ?: 0.0F).toInt()}%")
            views.setTextViewText(R.id.pressureWidget, "$pressure hPa")
            views.setTextViewText(R.id.tempWidget, "${temp?.toInt()}°C")
            views.setTextViewText(R.id.widgetWeatherType, "$weatherDescription")
            views.setImageViewResource(R.id.widgetWeatherIcon, weatherIcon ?: defaultIcon)
            views.setImageViewResource(R.id.widgetWindSpd, windIcon ?: defaultIcon)
            views.setImageViewResource(R.id.widgetRefresh, R.drawable.wi_refresh)

            views.setImageViewBitmap(R.id.widgetWindDir, bmpResult)
        }
        (fR.getById(now+1) ?: empty).run {
            views.setTextViewText(R.id.wlTemp, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+2) ?: empty).run {
            views.setTextViewText(R.id.wlTemp1, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon1, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+3) ?: empty).run {
            views.setTextViewText(R.id.wlTemp2, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon2, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+4) ?: empty).run {
            views.setTextViewText(R.id.wlTemp3, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon3, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+5) ?: empty).run {
            views.setTextViewText(R.id.wlTemp4, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon4, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+6) ?: empty).run {
            views.setTextViewText(R.id.wlTemp5, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon5, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+7) ?: empty).run {
            views.setTextViewText(R.id.wlTemp6, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon6, weatherIcon ?: defaultIcon)
        }
        (fR.getById(now+8) ?: empty).run {
            views.setTextViewText(R.id.wlTemp7, "${temp?.toInt()}°C")
            views.setImageViewResource(R.id.wlIcon7, weatherIcon ?: defaultIcon)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

