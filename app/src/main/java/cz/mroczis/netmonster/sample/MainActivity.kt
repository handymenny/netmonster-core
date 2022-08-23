package cz.mroczis.netmonster.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.ServiceState
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.merge.CellSource
import cz.mroczis.netmonster.core.feature.postprocess.CellPostprocessor
import cz.mroczis.netmonster.core.model.connection.PrimaryConnection
import cz.mroczis.netmonster.sample.databinding.ActivityMainBinding

/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REFRESH_RATIO = 5_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val adapter = MainAdapter()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

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

            Log.d("NTM-RES", " \n${merged.joinToString(separator = "\n")}")
        }
    }

    private fun onShare() {
        val shareText = Intent(Intent.ACTION_SEND)
        shareText.type = "text/plain"
        shareText.putExtra(Intent.EXTRA_SUBJECT, "netcore")
        shareText.putExtra(Intent.EXTRA_TEXT, getDataToShare())
        startActivity(Intent.createChooser(shareText, "Share"))
    }

    @SuppressLint("MissingPermission")
    private fun getDataToShare(): String {
        val context = this
        var dataToShare = "NetmonsterPostProcess\n${
            adapter.data.joinToString(separator = "\n")
        }"
        val subscriptions = NetMonsterFactory.getSubscription(context).getActiveSubscriptionIds()
        subscriptions.ifEmpty { listOf(255) }.forEach { sub ->
            val telephony = NetMonsterFactory.getTelephony(context, sub)
            dataToShare += "\n\nNetworkOperator$sub\n${
                telephony.getNetworkOperator()
            }"
            dataToShare += "\n\nRawCellInfo$sub\n${
                telephony.getAllCellInfo().joinToString(separator = "\n")
            }"
            dataToShare += "\n\nRawCellLocation$sub\n${
                telephony.getCellLocation().joinToString(separator = "\n")
            }"
        }
        for(x in  CellPostprocessor.values().toList()) {
            NetMonsterFactory.get(this).apply {
                val merged = getCells(CellSource.ALL_CELL_INFO, postprocessors = listOf(x))
                dataToShare += "\n\nPostProcessor${x.name}\n${
                    merged.filter {
                        it.connectionStatus == PrimaryConnection()
                    }.joinToString(separator = "\n")
                }"
            }
        }
        return dataToShare
    }
}
