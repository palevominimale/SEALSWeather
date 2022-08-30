package my.weather.data

import android.content.Context

class ForecastRepository(context: Context) {

    private var db: DAO = DBForecast.getInstance(context)?.DAO()!!

    fun getById(id: Int): ForecastItem? {
        return db.findById(id)
    }

    fun getByIds(ids: List<Int>): List<ForecastItem?> {
        return db.loadAllByIds(ids)
    }

    fun getByRange(ids: IntRange) : List<ForecastItem?>{
        return db.loadByRange(ids.toList())
    }

    fun insertItem(item: ForecastItem) {
        db.insertItem(item)
    }


    fun deleteAll() {
        db.deleteAll()
    }

}