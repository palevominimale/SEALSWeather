package my.weather.data

import androidx.room.*

@Dao
interface DAO {
    @Query("SELECT * FROM Forecast")
    fun getAll(): List<ForecastItem?>

    @Query("SELECT * FROM Forecast WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: List<Int>): List<ForecastItem?>

    @Query("SELECT * FROM Forecast WHERE id IN (:userIds)")
    fun loadByRange(userIds: List<Int>): List<ForecastItem?>

    @Query("SELECT * FROM Forecast WHERE id LIKE :id LIMIT 1")
    fun findById(id: Int): ForecastItem?

    @Update
    fun updateItem(item: ForecastItem)

    @Insert
    fun insertItem(item: ForecastItem)

    @Insert
    fun insertAll(vararg items: ForecastItem)

    @Delete
    fun delete(item: ForecastItem)

    @Query("DELETE FROM Forecast")
    fun deleteAll()
}