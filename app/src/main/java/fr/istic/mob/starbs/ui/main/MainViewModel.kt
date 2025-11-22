package fr.istic.mob.starbs.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.istic.mob.starbs.MainApp
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    fun resetDatabase() {
        viewModelScope.launch {
            MainApp.repository.clearDatabase()
        }
    }

    fun downloadGTFS(context: android.content.Context) {
        val intent = android.content.Intent(
            context,
            fr.istic.mob.starbs.services.GTFSDownloaderService::class.java
        )
        context.startService(intent)
    }
}
