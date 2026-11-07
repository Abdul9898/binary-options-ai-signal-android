package com.binaryoptions.aisignal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.binaryoptions.aisignal.adapter.SignalAdapter
import com.binaryoptions.aisignal.databinding.ActivityMainBinding
import com.binaryoptions.aisignal.engine.SignalEngine
import com.binaryoptions.aisignal.service.SignalBackgroundService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SignalAdapter
    private var isSoundEnabled = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            SignalBackgroundService.startService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        checkPermissions()

        // Start background service & engine
        SignalBackgroundService.startService(this)
        observeSignalEngine()
    }

    private fun setupRecyclerView() {
        adapter = SignalAdapter(SignalEngine.instance.signals)
        binding.rvSignals.layoutManager = LinearLayoutManager(this)
        binding.rvSignals.adapter = adapter
    }

    private fun setupListeners() {
        // Swipe to refresh
        binding.swipeRefresh.setColorSchemeResources(R.color.accent_purple, R.color.signal_call)
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_card)
        binding.swipeRefresh.setOnRefreshListener {
            SignalEngine.instance.scanNow()
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Scanning pairs for new setups...", Toast.LENGTH_SHORT).show()
        }

        // Manual scan button
        binding.btnScan.setOnClickListener {
            SignalEngine.instance.scanNow()
            Toast.makeText(this, "⚡ AI Scan in progress...", Toast.LENGTH_SHORT).show()
        }

        // Broker button
        binding.btnBroker.setOnClickListener {
            val brokerUrl = "https://pocketoption.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(brokerUrl))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Opening broker: $brokerUrl", Toast.LENGTH_SHORT).show()
            }
        }

        // Telegram button
        binding.btnTelegram.setOnClickListener {
            val telegramUrl = "https://t.me/SignalBotsai"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Opening Telegram channel...", Toast.LENGTH_SHORT).show()
            }
        }

        // Sound Toggle
        binding.btnSoundToggle.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            binding.btnSoundToggle.text = if (isSoundEnabled) "🔔 Alert: ON" else "🔕 Alert: OFF"
            Toast.makeText(
                this,
                if (isSoundEnabled) "Alert sound enabled" else "Alert sound muted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeSignalEngine() {
        val engine = SignalEngine.instance

        engine.onNewSignalListener = { signal ->
            runOnUiThread {
                adapter.updateList(engine.signals)
                binding.tvSignalCount.text = "${engine.signals.size} Signals"

                if (isSoundEnabled) {
                    playChime()
                }
            }
        }

        engine.onSignalUpdatedListener = { _ ->
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }

        engine.onStatsUpdatedListener = { winRate, won, lost, streak ->
            runOnUiThread {
                binding.tvWinRate.text = "$winRate%"
                binding.tvStreak.text = "🔥 $streak WIN STREAK"
                binding.tvStatsSummary.text = "$won Wins • $lost Losses (Total ${won + lost} Trades)"
            }
        }

        engine.onConnectionStateListener = { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    binding.tvLiveStatus.text = "LIVE FEED"
                    binding.tvLiveStatus.setTextColor(ContextCompat.getColor(this, R.color.signal_call))
                } else {
                    binding.tvLiveStatus.text = "RECONNECTING"
                    binding.tvLiveStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_gold))
                }
            }
        }

        // Initial sync
        binding.tvSignalCount.text = "${engine.signals.size} Signals"
    }

    private fun playChime() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()

            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(150)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
