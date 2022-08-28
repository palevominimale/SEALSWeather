package my.weather.network

import android.content.Context
import android.location.Location
import android.util.Log
import my.weather.R
import my.weather.data.ForecastItem
import my.weather.data.ForecastRepository
import my.weather.interaction.IntentHelpers
import my.weather.logic.FORECAST_DEPTH
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.time.*
import java.time.format.DateTimeFormatter

private lateinit var openMeteoData: String
private lateinit var urlOpenMeteo: URL

@Suppress("UNNECESSARY_SAFE_CALL")
class NetworkOperations(val context: Context, private val fr: ForecastRepository) {
    var lat = 46.4887
    var lon = 3.9939
    private val iH = IntentHelpers(context)
    private val timeZone = ZoneId.systemDefault().id

    fun setLocation(location: Location) {
        lat = location.latitude
        lon = location.longitude

    }

    fun getOpenMeteoAPIData() {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateStart = LocalDateTime.now()
        val dateEnd: LocalDateTime = LocalDateTime.now().plusHours(FORECAST_DEPTH)
        val dateStartFormatted = dateStart.format(format).toString()
        val dateEndFormatted = dateEnd.format(format).toString()
        urlOpenMeteo = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&hourly=temperature_2m,relativehumidity_2m,dewpoint_2m,apparent_temperature,surface_pressure,precipitation,weathercode,cloudcover,shortwave_radiation,windspeed_10m,winddirection_10m,windgusts_10m&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_hours&windspeed_unit=ms&timeformat=unixtime&timezone=$timeZone&start_date=$dateStartFormatted&end_date=$dateEndFormatted")
        openMeteoData = urlOpenMeteo.readText()
        putDataFromApiToDB(openMeteoData)
    }

    fun checkDB() {
        if(fr.getById(0)?.id == null) {
            getOpenMeteoAPIData()
            iH.notifyHaveNewData()
        }
    }

    private fun putDataFromApiToDB(json: String) {
        fr.deleteAll()
        try {
            val jsonHourly = JSONObject(json).getString("hourly")
            val jsonDaily = JSONObject(json).getString("daily")

            for(i in 0..FORECAST_DEPTH.toInt()) {

                val time = JSONObject(jsonHourly).getJSONArray("time")[i]
                    .toString().toLongOrNull() ?: 0L
                val temp = JSONObject(jsonHourly).getJSONArray("temperature_2m")[i]
                    .toString().toFloatOrNull() ?: 0.0F
                val pressure = JSONObject(jsonHourly).getJSONArray("surface_pressure")[i]
                    .toString().toFloatOrNull() ?: 0.0F
                val humidity = JSONObject(jsonHourly).getJSONArray("relativehumidity_2m")[i]
                    .toString().toFloatOrNull() ?: 0.0F
                val windSpd = JSONObject(jsonHourly).getJSONArray("windspeed_10m")[i]
                    .toString().toFloatOrNull() ?: 0.0F
                val windIconRotation = JSONObject(jsonHourly).getJSONArray("winddirection_10m")[i]
                    .toString().toFloatOrNull() ?: 0.0F
                val weatherCode = JSONObject(jsonHourly).getJSONArray("weathercode")[i]
                    .toString().toIntOrNull() ?: 0
                val tempMin = JSONObject(jsonDaily).getJSONArray("temperature_2m_min")[i/24]
                    .toString().toFloatOrNull() ?: 0.0F
                val tempMax = JSONObject(jsonDaily).getJSONArray("temperature_2m_max")[i/24]
                    .toString().toFloatOrNull() ?: 0.0F
                val sunrise = LocalDateTime.ofEpochSecond(
                    JSONObject(jsonDaily).getJSONArray("sunrise")[i/24].toString().toLong(),
                    0,
                    ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC).toString().toLong()
                val sunset = LocalDateTime.ofEpochSecond(
                    JSONObject(jsonDaily).getJSONArray("sunset")[i/24].toString().toLong(),
                    0,
                    ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC).toString().toLong()
                val weatherIcon = selectWeatherIcon(weatherCode, time)
                val windIcon = selectWindIcon(windSpd)
                val weatherDescription = selectWeatherType(weatherCode)

                fr.insertItem(ForecastItem(i, time, temp, tempMin, tempMax, humidity, pressure,
                    sunset, sunrise, weatherDescription, windIcon, windIconRotation, weatherIcon))
            }
        } catch (e: JSONException) {
            Log.println(Log.DEBUG,"JSON", e.toString())
        }
    }

    private fun selectWeatherIcon(weatherCode: Int, date: Long) : Int {

        val daytime = if(LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.UTC).hour in 8..19) "d" else "n"
        if(daytime == "n") {
            return when (weatherCode) {
                0 -> R.drawable.wi_stars
                1 -> R.drawable.wi_night_alt_cloudy_high
                2 -> R.drawable.wi_night_alt_partly_cloudy
                3 -> R.drawable.wi_night_alt_cloudy
                45, 48	-> R.drawable.wi_night_fog
                51 -> R.drawable.wi_night_alt_rain_mix
                53 -> R.drawable.wi_night_alt_rain_mix
                55 -> R.drawable.wi_night_alt_rain_mix
                56 -> R.drawable.wi_night_alt_rain_mix
                57 -> R.drawable.wi_night_alt_rain_mix
                61 -> R.drawable.wi_night_alt_rain
                63 -> R.drawable.wi_night_alt_rain
                65 -> R.drawable.wi_night_alt_rain
                66 -> R.drawable.wi_night_alt_rain_mix
                67 -> R.drawable.wi_night_alt_rain_mix
                71 -> R.drawable.wi_night_alt_snow
                73 -> R.drawable.wi_night_alt_snow
                75 -> R.drawable.wi_night_alt_snow
                77 -> R.drawable.wi_night_alt_hail
                80 -> R.drawable.wi_night_alt_showers
                81 -> R.drawable.wi_night_alt_showers
                82 -> R.drawable.wi_night_alt_showers
                85 -> R.drawable.wi_night_alt_snow
                86 -> R.drawable.wi_night_alt_snow
                95 -> R.drawable.wi_night_alt_lightning
                96 -> R.drawable.wi_night_alt_thunderstorm
                99 -> R.drawable.wi_night_alt_thunderstorm
                else -> R.drawable.wi_meteor
            }
        }else {
            return when(weatherCode) {
                0 -> R.drawable.wi_day_sunny
                1 -> R.drawable.wi_day_cloudy_high
                2 -> R.drawable.wi_day_sunny_overcast
                3 -> R.drawable.wi_day_cloudy
                45, 48	-> R.drawable.wi_night_fog
                51 -> R.drawable.wi_day_rain_mix
                53 -> R.drawable.wi_day_rain_mix
                55 -> R.drawable.wi_day_rain_mix
                56 -> R.drawable.wi_day_rain_mix
                57 -> R.drawable.wi_day_rain_mix
                61 -> R.drawable.wi_day_rain
                63 -> R.drawable.wi_day_rain
                65 -> R.drawable.wi_day_rain
                66 -> R.drawable.wi_day_rain_mix
                67 -> R.drawable.wi_day_rain_mix
                71 -> R.drawable.wi_day_snow
                73 -> R.drawable.wi_day_snow
                75 -> R.drawable.wi_day_snow
                77 -> R.drawable.wi_day_hail
                80 -> R.drawable.wi_day_showers
                81 -> R.drawable.wi_day_showers
                82 -> R.drawable.wi_day_showers
                85 -> R.drawable.wi_day_snow
                86 -> R.drawable.wi_day_snow
                95 -> R.drawable.wi_day_lightning
                96 -> R.drawable.wi_day_thunderstorm
                99 -> R.drawable.wi_day_thunderstorm
                else -> R.drawable.wi_meteor
            }
        }
    }

    private fun selectWindIcon(windIntensity: Float): Int {
        return when(windIntensity) {
            in 0.0..0.99 -> R.drawable.wi_wind_beaufort_0
            in 1.0..1.99 -> R.drawable.wi_wind_beaufort_1
            in 2.0..2.99 -> R.drawable.wi_wind_beaufort_2
            in 3.0..3.99 -> R.drawable.wi_wind_beaufort_3
            in 4.0..4.99 -> R.drawable.wi_wind_beaufort_4
            in 5.0..5.99 -> R.drawable.wi_wind_beaufort_5
            in 6.0..6.99 -> R.drawable.wi_wind_beaufort_6
            in 7.0..7.99 -> R.drawable.wi_wind_beaufort_7
            in 8.0..8.99 -> R.drawable.wi_wind_beaufort_8
            in 9.0..9.99 -> R.drawable.wi_wind_beaufort_9
            in 10.0..10.99 -> R.drawable.wi_wind_beaufort_10
            in 11.0..11.99 -> R.drawable.wi_wind_beaufort_11
            in 12.0..12.99 -> R.drawable.wi_wind_beaufort_12
            else -> R.drawable.wi_strong_wind
        }
    }

    private fun selectWeatherType(weatherCode: Int) : String {
        return when(weatherCode) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48	-> "Fog"
            51 -> "Light drizzle"
            53 -> "Moderate drizzle"
            55 -> "Dense drizzle"
            56 -> "Freezing drizzle"
            57 -> "Freezing drizzle"
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            66 -> "Freeing rain"
            67 -> "Freezing rain"
            71 -> "Slight snowfall"
            73 -> "Moderate snowfall"
            75 -> "Heavy snowfall"
            77 -> "Snow grains"
            80 -> "Slight rain shower"
            81 -> "Moderate rain shower"
            82 -> "Violent rain shower"
            85 -> "Slight snow shower"
            86 -> "Heavy snow shower"
            95 -> "Thunderstorm"
            96 -> "Thunderstorm, hail"
            99 -> "Thunderstorm, hail"
            else -> "Aliens invasion!"
        }
    }
}