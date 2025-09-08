package com.example.kodomo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.kodomo.MainActivity.GlobalVariables
import com.example.kodomo.data.BacDataclassDb
import com.example.kodomo.data.BacKodomoDatabase
import com.example.kodomo.data.BacKodomoRepository
import com.example.kodomo.data.DataclassDb
import com.example.kodomo.data.KodomoDatabase
import com.example.kodomo.data.KodomoRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class TrackerLoop(
    private val context: Context,

    private val logCallback: (String) -> Unit
) {
    private val serverUrl = "https://gpstrackerbackend-du04.onrender.com/"
    //private val serverUrl = "http://192.168.0.102:5000/"
    // CSV logging is optional if you decide to switch entirely to Room DB.
    //private val csvFile = "localtracked.csv"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private var serverUp = false
    private var running = false
    private var lastServerUp = serverUp
    val db = KodomoDatabase.getDatabase(context)
    val repo = KodomoRepository(db.coordinateLogDao())
    val bdb = BacKodomoDatabase.bacgetDatabase(context)
    val bacrepo = BacKodomoRepository(bdb.coordinateLogDao())
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun startLoop() {
        initLocationClient()
        running = true
        while (running) {
            val coord = getCurrentLocation()
            checkServerStatus()
            if (serverUp && !lastServerUp) {
                val synced = syncUnsent()
                logCallback("Synced backup logs: $synced")
            }
            lastServerUp = serverUp
            val statusMsg: String = if (serverUp) {
                // When the server is up, try to send the coordinate.
                if (sendCoordsToServer(listOf(coord))) {
                    logCoordinateToDatabase(coord)
                    "Sent to server: $coord \n\n"

                } else {
                    // If sending fails, log it in the Room database.
                    logCoordinateToDatabase(coord)
                    "Send failed. Logged to database: $coord \n\n"
                }
            } else {
                // When the server is down, immediately log the coordinate.
                logCoordinateToDatabase(coord)
                "Server down. Logged to database: $coord \n\n"
            }
            logCallback(statusMsg)
            delay(1000)
        }
    }

    fun stopLoop() {
        running = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): JSONObject {
        return withContext(Dispatchers.IO) {
            val deferred = kotlinx.coroutines.CompletableDeferred<Location?>()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    deferred.complete(result.lastLocation)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
            val location = deferred.await()

            val json = JSONObject()
            val timestamp = Instant.now().toString()

            if (location != null) {
                json.put("x_cord", location.latitude)
                json.put("y_cord", location.longitude)
            } else {
                json.put("x_cord", JSONObject.NULL)
                json.put("y_cord", JSONObject.NULL)
            }
            json.put("logged_time", timestamp)
            json
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun checkServerStatus() {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val req = Request.Builder().url("$serverUrl/serverstatus").build()
            try {
                val resp = client.newCall(req).execute()
                val bodyStr = resp.body?.string()
                // Expect a JSON response with a boolean field "server".
                serverUp = resp.code == 200 && JSONObject(bodyStr ?: "{}").optBoolean("server", false)
            } catch (_: Exception) {
                serverUp = false
            }
            val timestamp = Instant.now().toString()
            val statusStr = if (serverUp) "server_up" else "server_down"
            val logLine = "$statusStr, $timestamp"
            logCallback(logLine)
        }
    }

    suspend fun sendCoordsToServer(coords: List<JSONObject>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val arr = JSONArray()
                coords.forEach { arr.put(it) }
                val jsonBody = JSONObject()
                jsonBody.put("firebaseid", GlobalVariables.firebaseid)
                jsonBody.put("coords", arr)
                val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$serverUrl/send")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                response.code == 200
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun logCoordinateToDatabase(coord: JSONObject) {
        withContext(Dispatchers.IO) {
            val x = coord.optDouble("x_cord")
            val y = coord.optDouble("y_cord")
            val time = coord.optString("logged_time")
            if (serverUp) {
                val coordinateLog = DataclassDb(xCord = x, yCord = y, loggedTime = time)
                repo.addOrUpdateLog(coordinateLog)
            }
            if (!serverUp){
                val baccoordinateLog = BacDataclassDb(xCord = x, yCord = y, loggedTime = time)
                bacrepo.addOrUpdateLog(baccoordinateLog)
                val coordinateLog = DataclassDb(xCord = x, yCord = y, loggedTime = time)
                repo.addOrUpdateLog(coordinateLog)
            }
        }
    }

    suspend fun getAllLogsFromDatabase(): List<DataclassDb> {
        return withContext(Dispatchers.IO) {
            repo.getAllLogs()
        }
    }

    suspend fun syncUnsent(): Boolean {
        return withContext(Dispatchers.IO) {
            val logsToSend = bacrepo.getAllLogs()
            if (logsToSend.isEmpty()) return@withContext true

            val jsonObjectsToSend = logsToSend.map { bacLog ->
                JSONObject().apply {
                    put("x_cord", bacLog.xCord)
                    put("y_cord", bacLog.yCord)
                    put("logged_time", bacLog.loggedTime)
                }
            }
            val success = sendCoordsToServer(jsonObjectsToSend)
            if (success) {
                logsToSend.forEach { bacrepo.deleteLog(it) }
            } else {
                Log.d("SyncLogs", "Failed to sync logs to server. Retaining in backup DB.")
            }

            success
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()
    }


    fun isRunning(): Boolean {
        return running
    }

}
