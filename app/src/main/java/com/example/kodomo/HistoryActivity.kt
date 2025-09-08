package com.example.kodomo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kodomo.data.DataclassDb
import com.example.kodomo.data.KodomoDatabase
import com.example.kodomo.data.KodomoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HistoryActivity : AppCompatActivity() {
    private lateinit var spinner: Spinner
    private lateinit var expandableListView: ExpandableListView
    private lateinit var repo: KodomoRepository
    private val groupedLogs = mutableMapOf<String, List<DataclassDb>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        spinner = findViewById(R.id.spinnerDates)
        expandableListView = findViewById(R.id.expandableListView)
        val db = KodomoDatabase.getDatabase(this)
        repo = KodomoRepository(db.coordinateLogDao()) // FIX: remove shadowing

        lifecycleScope.launch {
            val allDates = repo.getAllDays()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@HistoryActivity, android.R.layout.simple_spinner_item, allDates)
                spinner.adapter = adapter
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (allDates.isNotEmpty() && position < allDates.size) { // Add a safety check for allDates
                        val selectedDate = allDates[position]
                        // Launch a coroutine using lifecycleScope
                        lifecycleScope.launch {
                            try {
                                showLogsForDate(selectedDate)
                            } catch (e: Exception) {
                                // Handle any exceptions from showLogsForDate, e.g., log them
                                Log.e("HistoryActivity", "Error loading logs for date: $selectedDate", e)
                                // Optionally, show a Toast to the user
                                Toast.makeText(this@HistoryActivity, "Error loading logs", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.w("HistoryActivity", "allDates is empty or position is out of bounds in onItemSelected.")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Optionally, clear the list or handle this case
                    lifecycleScope.launch {
                        try {
                            // Example: Clear the list if nothing is selected
                            // showLogsForDate(null) // You'd need to modify showLogsForDate to handle null
                            // Or clear the adapter directly:
                            expandableListView.setAdapter(null as? ExpandableListAdapter)
                        } catch (e: Exception) {
                            Log.e("HistoryActivity", "Error in onNothingSelected", e)
                        }
                    }
                }
            }
        }

    }

    private suspend fun showLogsForDate(date: String) {
        val logs = repo.getAllLogs()  // ✅ fetch logs, not dates
            .filter { it.simdate == date }  // ✅ filter logs for selected date

        val grouped = logs.groupBy {
            try {
                val hourStr = it.loggedTime.substring(11, 13)  // e.g., "14" from "2024-06-01 14:05:00"
                "${hourStr}h"
            } catch (e: Exception) {
                "Unknown"
            }
        }

        withContext(Dispatchers.Main) {
            val adapter = HistoryExpandableAdapter(this@HistoryActivity, grouped)
            expandableListView.setAdapter(adapter)
        }
    }

}
