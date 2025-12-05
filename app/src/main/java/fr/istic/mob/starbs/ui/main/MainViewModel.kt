package fr.istic.mob.starbs.ui.main

import android.app.Application
import android.content.*
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
