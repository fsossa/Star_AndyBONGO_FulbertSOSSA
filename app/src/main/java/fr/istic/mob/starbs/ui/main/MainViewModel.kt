package fr.istic.mob.starbs.ui.main

import android.app.Application
import android.content.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.istic.mob.starbs.services.GTFSParserService

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _progressPercent = MutableLiveData(0)
    val progressPercent: LiveData<Int> = _progressPercent

    private val _progressMessage = MutableLiveData("Initialisation…")
    val progressMessage: LiveData<String> = _progressMessage

    private val _isReady = MutableLiveData(false)
    val isReady: LiveData<Boolean> = _isReady

    // --------------------
    // USER SELECTION : DATE & TIME
    // --------------------
    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _selectedTime = MutableLiveData<String>()
    val selectedTime: LiveData<String> = _selectedTime

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedTime(time: String) {
        _selectedTime.value = time
    }

    init {
        val now = java.util.Calendar.getInstance()
        val d = now.get(java.util.Calendar.DAY_OF_MONTH)
        val m = now.get(java.util.Calendar.MONTH) + 1
        val y = now.get(java.util.Calendar.YEAR)

        val h = now.get(java.util.Calendar.HOUR_OF_DAY)
        val min = now.get(java.util.Calendar.MINUTE)

        _selectedDate.value = "%02d/%02d/%04d".format(d, m, y)
        _selectedTime.value = "%02d:%02d".format(h, min)
    }


    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == GTFSParserService.ACTION_PROGRESS) {
                val percent = intent.getIntExtra(GTFSParserService.EXTRA_PERCENT, 0)
                val msg = intent.getStringExtra(GTFSParserService.EXTRA_MESSAGE) ?: ""

                _progressPercent.value = percent
                _progressMessage.value = msg

                if (percent >= 100) {
                    _isReady.value = true
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun registerReceiver() {
        val filter = IntentFilter(GTFSParserService.ACTION_PROGRESS)
        getApplication<Application>().registerReceiver(
            progressReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregisterReceiver() {
        getApplication<Application>().unregisterReceiver(progressReceiver)
    }
}
