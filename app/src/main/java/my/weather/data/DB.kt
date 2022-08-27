package my.weather.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ForecastItem::class], version = 1)
abstract class DBForecast : RoomDatabase() {

    abstract fun DAO(): DAO

    companion object {
        private var INSTANCE: DBForecast? = null

        fun getInstance(context: Context): DBForecast? {
            if (INSTANCE == null) synchronized(DBForecast::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    DBForecast::class.java,
                    "forecast.db"
                    ).allowMainThreadQueries()
                    .build()
                }
            return INSTANCE
        }

    }
}