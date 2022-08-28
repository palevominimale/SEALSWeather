package my.weather.logic

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayout
import my.weather.databinding.ActivityMainBinding
import my.weather.interaction.RefreshData
import java.util.concurrent.TimeUnit

const val FORECAST_DEPTH = 168L
const val HOUR_OF_INTEREST = 15
const val NEW_DATA_AVAILABLE = "NEW_DATA_AVAILABLE"
const val NEED_DATA = "NEED_DATA"
const val REFRESHING = "REFRESHING"
const val READY = "READY"
const val SELF_REFRESH = "SELF_REFRESH"
const val REFRESHED = "REFRESHED"
const val APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE"
var locationPermissions = false

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        val sPAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sPAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        setupIntentManager()
    }

    override fun onDestroy() {
        super.onDestroy()
        WorkManager.getInstance(this).cancelAllWork()
    }

    private fun setupIntentManager() {
        val filter = IntentFilter().apply {
            addAction(READY)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    READY -> {
                        setupWorkRequest()
                        Log.println(Log.DEBUG, "IM", "(MAIN) Work initialized!")
                    }
                }
            }
        }
        registerReceiver(receiver, filter)
        Log.println(Log.DEBUG, "IM", "(MAIN) Registered!")
    }

    private fun setupWorkRequest() {
        WorkManager.getInstance(this).cancelAllWork()
        val wR = PeriodicWorkRequest.Builder(RefreshData::class.java, 15, TimeUnit.MINUTES).setInitialDelay(20, TimeUnit.SECONDS).build()
        WorkManager.getInstance(this).enqueue(wR)
    }

    private fun checkPermissions():Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            locationPermissions = false
            false
        } else {
            locationPermissions = true
            true
        }
    }
}