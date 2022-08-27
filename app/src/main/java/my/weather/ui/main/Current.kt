package my.weather.ui.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.weather.adapters.HourlyAdapter
import my.weather.data.ForecastItem
import my.weather.data.ForecastRepository
import my.weather.databinding.FragmentCurrentBinding
import my.weather.interaction.IntentHelpers
import my.weather.interaction.RefreshData
import my.weather.logic.*
import my.weather.network.NetworkOperations
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

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
                loadRecyclerToView()
                loadWeatherToView()
                requireActivity().runOnUiThread {
                    binding.hourlyRecycler.adapter?.notifyDataSetChanged()
                }
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
//        loadWeatherToView()
        Log.println(Log.DEBUG, "RESUMED (Current)", Instant.now().toString())
//        loadRecyclerToView()
    }

    override fun onDestroy() {
        super.onDestroy()
        WorkManager.getInstance(requireContext()).cancelAllWork()
    }

    private fun setupWorkRequest() {
        WorkManager.getInstance(requireContext()).cancelAllWork()
        val wR = PeriodicWorkRequest.Builder(RefreshData::class.java, 15, TimeUnit.MINUTES).setInitialDelay(20, TimeUnit.SECONDS).build()
        WorkManager.getInstance(requireContext()).enqueue(wR)
    }

    private fun setupIntentManager() {
        val filter = IntentFilter().apply {
            addAction(REFRESHING)
            addAction(REFRESHED)
            addAction(NEED_DATA)
            addAction(READY)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    NEED_DATA -> {
                        requireActivity().runOnUiThread {
                            reloadAll()
                            Log.println(Log.DEBUG, "IM", "(CURRENT) Received: $intent")
                        }
                    }
                    REFRESHING -> {
                        requireActivity().runOnUiThread {
                            binding.swipeRefreshCurrent.isEnabled = false
                            Log.println(Log.DEBUG, "IM", "(CURRENT) Swipe blocked!: $intent")
                        }
                    }
                    REFRESHED -> {
                        requireActivity().runOnUiThread {
                            loadRecyclerToView()
                            loadWeatherToView()
                            binding.swipeRefreshCurrent.isRefreshing = false
                            binding.swipeRefreshCurrent.isEnabled = true
                            Log.println(Log.DEBUG, "IM", "(CURRENT) Swipe unblocked!: $intent")
                        }
                    }
                    READY -> setupWorkRequest()
                }
            }
        }
        requireActivity().registerReceiver(receiver, filter)
        Log.println(Log.DEBUG, "IM", "(CURRENT) Registered!")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun reloadAll() {
        CoroutineScope(Dispatchers.IO).launch {
            network.getOpenMeteoAPIData()
        }.invokeOnCompletion {
            loadRecyclerToView()
            requireActivity().runOnUiThread {
                loadWeatherToView()
                binding.hourlyRecycler.adapter?.notifyDataSetChanged()

            }
            iH.notifyHaveNewData()
            iH.notifyReadyAfterRefreshing()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadRecyclerToView() {
        if(fR.getAll().isNotEmpty()) {
            hourlyForecast.clear()
            val now = LocalTime.now().hour
            for (i in now..now + 48) {
                hourlyForecast.add(fR.getById(i))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadWeatherToView() {
        val weather = fR.getById(LocalDateTime.now().hour)
        weather.run {
            val format = DateTimeFormatter.ofPattern("HH:mm")
            binding.temp.text = temp.toString() + "°C"
            binding.tempMax.text = tempMax.toString() + "°C"
            binding.tempMin.text = tempMin.toString() + "°C"
            binding.pressure.text = pressure.toString() + " hPa"
            binding.humidity.text = "RH: " + humidity.toString() +"%"
            binding.sunrise.text = Instant.ofEpochSecond(sunrise!!)
                .atZone(ZoneId.systemDefault()).format(format)
            binding.sunset.text = Instant.ofEpochSecond(sunset!!)
                .atZone(ZoneId.systemDefault()).format(format)
            binding.weatherType.text = weatherDescription
            binding.windCurrentDirection.rotation = windIconRotation!!
            binding.imageWeatherType.setImageResource(weatherIcon!!)
            binding.windCurrentIntensity.setImageResource(windIcon!!)
            binding.location.text = network.lon.toString() + " " + network.lat.toString()
        }
    }
}