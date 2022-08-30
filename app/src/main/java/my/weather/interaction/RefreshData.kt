package my.weather.interaction

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import my.weather.data.ForecastRepository
import my.weather.network.NetworkOperations

class RefreshData(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
    private val fR = ForecastRepository(context)
    private val iH = IntentHelpers(context)
    private val network = NetworkOperations(context, fR)
    override fun doWork(): Result {
        iH.notifyIsRefreshing()
        network.getOpenMeteoAPIData()
        iH.notifyReadyAfterRefreshing()
        return Result.success()
    }
}