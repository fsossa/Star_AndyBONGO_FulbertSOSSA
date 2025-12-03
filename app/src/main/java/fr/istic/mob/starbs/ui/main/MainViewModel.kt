package fr.istic.mob.starbs.ui.main

import android.content.Context
import androidx.lifecycle.*
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.utils.GTFSConstants
import fr.istic.mob.starbs.utils.GTFSUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel : ViewModel() {

    private val _routes = MutableLiveData<List<fr.istic.mob.starbs.data.local.entities.Route>>()
    val routes: LiveData<List<fr.istic.mob.starbs.data.local.entities.Route>> = _routes

    private val _directions = MutableLiveData<List<String>>()
    val directions: LiveData<List<String>> = _directions

    private val _times = MutableLiveData<List<String>>()
    val times: LiveData<List<String>> = _times

    private val _progressPercent = MutableLiveData<Int>()
    val progressPercent: LiveData<Int> = _progressPercent

    private val _progressMessage = MutableLiveData<String>()
    val progressMessage: LiveData<String> = _progressMessage

    private val _progress = MutableLiveData<Pair<Int, String>>()
    val progress: LiveData<Pair<Int, String>> = _progress

    fun updateProgress(percent: Int, message: String) {
        _progress.postValue(percent to message)
        _progressPercent.postValue(percent)
        _progressMessage.postValue(message)
        if (percent >= 100) {
            loadRoutes()
        }
    }

    fun loadRoutes() {
        viewModelScope.launch {
            _routes.value = MainApp.repository.getAllRoutes()
        }
    }

    fun loadDirections(routeId: String) {
        viewModelScope.launch {
            _directions.value = MainApp.repository.getDirectionsForRoute(routeId)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            try {
                MainApp.repository.clearDatabase()

                // On vide les LiveData côté UI pour éviter d'afficher des vieux trucs
                _routes.value = emptyList()
                _directions.value = emptyList()
                _times.value = emptyList()

                // Indiquer que c'est fini
                _progress.postValue(0 to "Base réinitialisée")
            } catch (e: Exception) {
                _progress.postValue(0 to "Erreur reset: ${e.message}")
            }
        }
    }


    fun downloadGTFS(context: Context) {
        val intent = android.content.Intent(
            context,
            fr.istic.mob.starbs.services.GTFSDownloaderService::class.java
        )
        context.startService(intent)
    }

    fun loadTimes(routeId: String, direction: String, date: String, time: String) {
        viewModelScope.launch {
            _times.value = MainApp.repository.getHoraires(routeId, direction, date, time)
        }
    }

    fun autoUpdateGTFS(context: Context) {
        viewModelScope.launch {
            val isEmpty = MainApp.repository.isDatabaseEmpty()

            val localJson = GTFSUtils.loadLocalJson(
                File(
                    context.filesDir,
                    GTFSConstants.LOCAL_JSON_FILE
                )
            )
            val localFinValidite = GTFSUtils.extractFinValidite(localJson)

            val expired = GTFSUtils.isGtfsExpired(localFinValidite)

            if (isEmpty || expired) {
                withContext(Dispatchers.Main) {
                    _progress.value = 0 to "Téléchargement JSON…"
                    downloadGTFS(context)
                }
            } else {
                // Base prête et à jour : on charge directement les routes
                withContext(Dispatchers.Main) {
                    loadRoutes()
                }
            }
        }
    }

    suspend fun isDatabaseReady(): Boolean =
        withContext(Dispatchers.IO) {
            !MainApp.repository.isDatabaseEmpty()
        }

}
