package my.weather.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.weather.R
import my.weather.data.ForecastItem
import java.time.Instant
import java.time.ZoneId

class HourlyAdapter(private val forecasts: MutableList<ForecastItem>) :
    RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recyclerviewTemp: TextView = itemView.findViewById(R.id.recyclerviewTemp)
        val recyclerviewPressure: TextView = itemView.findViewById(R.id.recyclerviewPressure)
        val recyclerviewHumidity: TextView = itemView.findViewById(R.id.recyclerviewHumidity)
        val recyclerviewWindDirection: ImageView = itemView.findViewById(R.id.windDirection)
        val recyclerviewWindIntensity: ImageView = itemView.findViewById(R.id.windIntensity)
        val recyclerviewWeatherIcon: ImageView = itemView.findViewById(R.id.recycleviewWeatherIcon)
        val recyclerviewHour: TextView = itemView.findViewById(R.id.recyclerviewHour)
        val recyclerviewMinute: TextView = itemView.findViewById(R.id.recyclerviewMinute)
        val recyclerviewDayOfWeek: TextView = itemView.findViewById(R.id.weekDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.forecast_item, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        forecasts[position].run {
            val date = Instant.ofEpochSecond(time).atZone(ZoneId.systemDefault())
            holder.recyclerviewHumidity.text = "RH: " + humidity.toString() + "%"
            holder.recyclerviewTemp.text = temp.toString() + "°C"
            holder.recyclerviewWindDirection.rotation = windIconRotation!!
            holder.recyclerviewWindIntensity.setImageResource(windIcon!!)
            holder.recyclerviewHour.text = date.toString().subSequence(11,13)
            holder.recyclerviewMinute.text = date.toString().subSequence(14,16)
            holder.recyclerviewPressure.text = pressure.toString() + " hPa"
            holder.recyclerviewWeatherIcon.setImageResource(weatherIcon!!)
            holder.recyclerviewDayOfWeek.text = date.dayOfWeek.toString().subSequence(0,3)
        }
    }

    override fun getItemCount(): Int = forecasts.size
}