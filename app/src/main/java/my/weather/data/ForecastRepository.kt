package my.weather.data

import android.content.Context

class ForecastRepository(context: Context) {

    private var db: DAO = DBForecast.getInstance(context)?.DAO()!!

    fun getAll(): List<ForecastItem> {
        return db.getAll()
    }
    fun getById(id: Int): ForecastItem {
        return db.findById(id)
    }

    fun insertItem(item: ForecastItem) {
        db.insertItem(item)
    }

    fun deleteAll() {
        db.deleteAll()
    }

}