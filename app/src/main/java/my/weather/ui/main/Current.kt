package my.weather.ui.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.weather.R
import my.weather.adapters.HourlyAdapter
import my.weather.data.ForecastItem
import my.weather.data.ForecastRepository
import my.weather.databinding.FragmentCurrentBinding
import my.weather.interaction.IntentHelpers
import my.weather.logic.*
import my.weather.network.NetworkOperations
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class Current : Fragment() {

    private var hourlyForecast = mutableListOf(ForecastItem())
    private lateinit var network: NetworkOperations
    private lateinit var binding: FragmentCurrentBinding
    private lateinit var fLC: FusedLocationProviderClient
    private lateinit var fR: ForecastRepository
    private lateinit var iH: IntentHelpers

    @SuppressLint("VisibleForTests", "MissingPermission", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fR = ForecastRepository(requireContext())
        fLC = FusedLocationProviderClient(requireActivity())
        iH = IntentHelpers(requireContext())
        network = NetworkOperations(requireContext(), fR)
        binding = FragmentCurrentBinding.inflate(layoutInflater)
        binding.hourlyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.hourlyRecycler.adapter = HourlyAdapter(hourlyForecast)

        CoroutineScope(Dispatchers.IO).launch {
            network.checkDB()
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                loadWeatherToView()
                loadRecyclerToView()
            }
        }

        binding.swipeRefreshCurrent.setOnRefreshListener {
            iH.notifyIsRefreshing()
            if(locationPermissions) {
                fLC.lastLocation.addOnSuccessListener {
                    network.setLocation(it!!)
                    reloadAll()
                }
            } else {
                reloadAll()
            }
        }
        setupIntentManager()
        iH.notifyReady()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadWeatherToView()
        loadRecyclerToView()
    }

    private fun setupIntentManager() {
        val filter = IntentFilter().apply {
            addAction(REFRESHING)
            addAction(REFRESHED)
            addAction(NEED_DATA)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    NEED_DATA -> {
                        requireActivity().runOnUiThread {
                            reloadAll()
                        }
                    }
                    REFRESHING -> {
                        requireActivity().runOnUiThread {
                            binding.swipeRefreshCurrent.isEnabled = false
                        }
                    }
                    REFRESHED -> {
                        requireActivity().runOnUiThread {
                            loadRecyclerToView()
                            loadWeatherToView()
                            binding.swipeRefreshCurrent.isRefreshing = false
                            binding.swipeRefreshCurrent.isEnabled = true
                        }
                    }
                }
            }
        }
        requireActivity().registerReceiver(receiver, filter)
    }

    private fun reloadAll() {
        CoroutineScope(Dispatchers.IO).launch {
            network.getOpenMeteoAPIData()
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                loadWeatherToView()
                loadRecyclerToView()
            }
            iH.notifyHaveNewData()
            iH.notifyReadyAfterRefreshing()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadRecyclerToView() {
        val check = fR.getById(0)?.time ?: 0L
        if(check != 0L) {
            hourlyForecast.clear()
            val now = LocalTime.now().hour
            for (i in now..now + 48) {
                hourlyForecast.add(fR.getById(i) ?: ForecastItem())
            }
        }
        requireActivity().runOnUiThread {
            binding.hourlyRecycler.adapter?.notifyDataSetChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadWeatherToView() {
        val weather = fR.getById(LocalDateTime.now().hour) ?: ForecastItem()
        weather.run {
            val city = Geocoder(context, Locale.ENGLISH)
                .getFromLocation(network.lat, network.lon, 1)[0].locality
            val format = DateTimeFormatter.ofPattern("HH:mm")
            binding.temp.text = "${(temp ?: 0.0F)}°C"
            binding.tempMax.text = "${(tempMax ?: 0.0F)}°C"
            binding.tempMin.text = "${(tempMin ?: 0.0F)}°C"
            binding.pressure.text = "${(pressure ?: 0.0F)} hPa"
            binding.humidity.text = "RH: ${(humidity ?: 0.0F)}%"
            binding.weatherType.text = weatherDescription
            binding.windCurrentDirection.rotation = windIconRotation ?: 0.0F
            binding.imageWeatherType.setImageResource(weatherIcon ?: R.drawable.wi_meteor)
            binding.windCurrentIntensity.setImageResource(windIcon ?: R.drawable.wi_wind_beaufort_1)
            binding.location.text = city
            binding.sunrise.text = Instant.ofEpochSecond(sunrise ?: 0L)
                .atZone(ZoneId.systemDefault()).format(format)
            binding.sunset.text = Instant.ofEpochSecond(sunset ?: 0L)
                .atZone(ZoneId.systemDefault()).format(format)
        }
    }
}