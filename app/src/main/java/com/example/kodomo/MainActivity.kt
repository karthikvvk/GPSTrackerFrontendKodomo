package com.example.kodomo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.kodomo.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    object GlobalVariables {
        var firebaseid: String? = null
        var googleId: String? = null

    }
    private var monitorJob: kotlinx.coroutines.Job? = null
    private lateinit var tvLog: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnRestart: Button
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button
    private var tracker: TrackerLoop? = null
    private var job: kotlinx.coroutines.Job? = null
    private val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var binding: ActivityMainBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    private fun checkAndRequestPermissions() {
        if (locationPermissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
        configureGoogleSignIn()
        GlobalVariables.firebaseid = auth.currentUser?.uid
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            //finish()
            return }
        GlobalVariables.firebaseid = auth.currentUser?.uid
        initializeMainUI()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeMainUI() {
        handleBatteryOptimization()
        setupUIComponents()
    }

    private fun handleBatteryOptimization() {
        val pm = getSystemService(PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUIComponents() {
        tvLog = binding.tvLocation
        btnLogout = binding.btnLogout
        btnStart = binding.btnStart
        btnStop = binding.btnStop
        btnRestart = binding.btnRestart
        btnHistory = binding.btnHistory

        tracker = TrackerLoop(applicationContext) { log ->
            runOnUiThread {
                tvLog.append(log + "\n")
            }
        }

        setupButtonListeners()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupButtonListeners() {
        btnStart.setOnClickListener {
            if (GlobalVariables.firebaseid.isNullOrEmpty()) {
                Toast.makeText(this, "Not signed in. Cannot start tracking.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val serviceIntent = Intent(this, LocationTrackingService::class.java)
            startForegroundService(serviceIntent)

            if (job?.isActive != true) {
                job = lifecycleScope.launch { tracker?.startLoop() }
                tvLog.append("🔄 Started logging...\n\n")
            }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            configureGoogleSignIn()
            ShouldLogout.set(true)
            googleSignInClient.revokeAccess().addOnCompleteListener {
                GlobalVariables.firebaseid = null
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        monitorJob = lifecycleScope.launch {
            tracker = TrackerLoop(applicationContext) { log ->
                runOnUiThread { tvLog.append("$log\n") }
            }
        }

        btnStop.setOnClickListener {
            val stopIntent = Intent(this, LocationTrackingService::class.java)
            stopService(stopIntent)
            tracker?.stopLoop()
            job?.cancel()
            tvLog.append("Stopped logging.\n\n")
        }

        btnRestart.setOnClickListener {
            if (GlobalVariables.firebaseid == "null") {
                Toast.makeText(this, "Not signed in. Cannot restart tracking.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tracker?.stopLoop()
            job?.cancel()
            tracker = TrackerLoop(applicationContext) { log ->
                runOnUiThread { tvLog.append(log + "\n") }
            }
            job = lifecycleScope.launch { tracker?.startLoop() }
            tvLog.append("🔄 Restarted logging...\n\n")
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

}
