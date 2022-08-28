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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.weather.adapters.DailyAdapter
import my.weather.data.ForecastItem
import my.weather.data.ForecastRepository
import my.weather.databinding.FragmentDailyBinding
import my.weather.interaction.IntentHelpers
import my.weather.logic.*
import my.weather.network.NetworkOperations
import java.time.Instant

@Suppress("SENSELESS_COMPARISON")
class Daily : Fragment() {

    private var dailyForecast = mutableListOf(ForecastItem())
    private lateinit var fR: ForecastRepository
    private lateinit var iH: IntentHelpers
    private lateinit var network: NetworkOperations
    private lateinit var binding: FragmentDailyBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        iH = IntentHelpers(requireContext())
        fR = ForecastRepository(requireContext())
        network = NetworkOperations(requireContext(), fR)
        binding = FragmentDailyBinding.inflate(layoutInflater)

        CoroutineScope(Dispatchers.IO).launch {
            loadRecyclerToView()
        }

        binding.dailyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.dailyRecycler.adapter = DailyAdapter(dailyForecast)
        binding.swipeRefreshDaily.setOnRefreshListener {
            iH.notifyIsRefreshing()
            loadRecyclerToView()
            iH.requestNewData()
            binding.dailyRecycler.adapter?.notifyDataSetChanged()
        }
        setupIntentManager()
        iH.notifyReady()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.println(Log.DEBUG, "RESUMED (Daily)", Instant.now().toString())
        loadRecyclerToView()
    }


    private fun setupIntentManager() {
        val filter = IntentFilter().apply {
            addAction(NEW_DATA_AVAILABLE)
            addAction(REFRESHED)
            addAction(REFRESHING)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    NEW_DATA_AVAILABLE -> {
                        requireActivity().runOnUiThread {
                            loadRecyclerToView()
                            Log.println(Log.DEBUG, "IM", "(DAILY) Received: $intent")
                        }
                    }
                    REFRESHING -> {
                        requireActivity().runOnUiThread {
                            binding.swipeRefreshDaily.isEnabled = false
                            Log.println(Log.DEBUG, "IM", "(DAILY) Swipe blocked!: $intent")
                        }
                    }
                    REFRESHED -> {
                        requireActivity().runOnUiThread {
                            loadRecyclerToView()
                            binding.swipeRefreshDaily.isRefreshing = false
                            binding.swipeRefreshDaily.isEnabled = true
                            Log.println(Log.DEBUG, "IM", "(DAILY) Swipe unblocked!: $intent")
                        }
                    }
                }
            }
        }
        requireActivity().registerReceiver(receiver, filter)
        Log.println(Log.DEBUG, "IM", "(DAILY) Registered!")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadRecyclerToView() {
        val check = fR.getById(0)?.time ?: 0L
        if(check != 0L) {
            dailyForecast.clear()
            for (i in HOUR_OF_INTEREST..FORECAST_DEPTH.toInt() step 24) {
                dailyForecast.add(fR.getById(i) ?: ForecastItem())
            }
            binding.dailyRecycler.adapter?.notifyDataSetChanged()
        }
    }
}