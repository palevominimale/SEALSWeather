package my.weather.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import my.weather.R

@Entity(tableName = "Forecast")
data class ForecastItem(
    @PrimaryKey val id: Int? = 0,
    @ColumnInfo(name = "time") val time: Long? = 0L,
    @ColumnInfo(name = "temp") val temp: Float? = 0.0F,
    @ColumnInfo(name = "tempMin") val tempMin: Float? = 0.0F,
    @ColumnInfo(name = "tempMax") val tempMax: Float? = 0.0F,
    @ColumnInfo(name = "humidity") val humidity: Float? = 0.0F,
    @ColumnInfo(name = "pressure") val pressure: Float? = 0.0F,
    @ColumnInfo(name = "sunset") val sunset: Long? = 0L,
    @ColumnInfo(name = "sunrise") val sunrise: Long? = 0L,
    @ColumnInfo(name = "weatherDescription") val weatherDescription: String? = "No data",
    @ColumnInfo(name = "windIcon") val windIcon: Int? = R.drawable.wi_meteor,
    @ColumnInfo(name = "windIconRotation") val windIconRotation: Float? = 0.0F,
    @ColumnInfo(name = "weatherIcon") val weatherIcon: Int? = R.drawable.wi_wind_beaufort_1
)