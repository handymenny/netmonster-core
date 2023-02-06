package cz.mroczis.netmonster.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import com.google.gson.GsonBuilder
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.sample.MainActivity.Companion.REFRESH_RATIO
import cz.mroczis.netmonster.sample.databinding.ActivityMainBinding
import cz.mroczis.netmonster.sample.storage.AppDatabase
import cz.mroczis.netmonster.sample.storage.Cell
import cz.mroczis.netmonster.sample.storage.CellDao
import cz.mroczis.netmonster.sample.storage.NetmonsterPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REFRESH_RATIO = 5_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val adapter = MainAdapter()
    private lateinit var dao: CellDao
    private lateinit var locationManager: LocationManager
    private lateinit var geocoder: Geocoder
    private val scope = CoroutineScope(Job() + Dispatchers.IO)


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        dao = AppDatabase.getDatabase(this).cellDao()
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        geocoder = Geocoder(this)
        with(binding) {
            setContentView(root)
            recycler.adapter = adapter
        }
        findViewById<Button>(R.id.search_button).setOnClickListener {
            onShare()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loop()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ), 0)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun loop() {
        updateData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }

    @SuppressLint("MissingPermission")
    private fun updateData() {
        NetMonsterFactory.get(this).apply {
            val merged = getCells()
            adapter.data = merged
            scope.launch {
                val location = getBestProvider()?.let {
                    locationManager.getLastKnownLocation(it)
                }
                var address: Address? = null
                if (location != null) {
                    try {
                        address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            ?.firstOrNull()
                    } catch (_: Exception) { }
                }
                val cellsToStore = merged.filter {
                    it.connectionStatus == PrimaryConnection()
                }.mapNotNull { iCell ->
                    Cell.valueOf(iCell).also {
                        it?.latitude = location?.latitude
                        it?.longitude = location?.longitude
                        it?.location = address?.getAddressLine(0)
                    }
                }
                dao.insertAll(cellsToStore)
            }
            Log.d("NTM-RES", " \n${merged.joinToString(separator = "\n")}")
        }
    }

    private fun onShare() {
        scope.launch {
            val shareText = Intent(Intent.ACTION_SEND)
            shareText.type = "text/plain"
            shareText.putExtra(Intent.EXTRA_SUBJECT, "netcore")
            shareText.putExtra(Intent.EXTRA_TEXT, getDataToShare())
            startActivity(Intent.createChooser(shareText, "Share"))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDataToShare(): String {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(
                NetmonsterPost(
                    dao.getAll()
                )
            )
    }

    @SuppressLint("InlinedApi")
    private fun getBestProvider(): String? {
        // Simple way to get the provider that most likely has an updated location
        val providers = locationManager.getProviders(true)
        if (providers.contains(LocationManager.FUSED_PROVIDER)) {
            return LocationManager.FUSED_PROVIDER
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER
        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER
        }
        return null
    }
}
