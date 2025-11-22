package fr.istic.mob.starbs.ui.main

import androidx.lifecycle.*
import fr.istic.mob.starbs.MainApp
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _routes = MutableLiveData<List<fr.istic.mob.starbs.data.local.entities.Route>>()
    val routes: LiveData<List<fr.istic.mob.starbs.data.local.entities.Route>> = _routes

    private val _directions = MutableLiveData<List<String>>()
    val directions: LiveData<List<String>> = _directions

    private val _times = MutableLiveData<List<String>>()
    val times: LiveData<List<String>> = _times


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

    fun loadTimes(routeId: String, direction: String, date: String) {
        viewModelScope.launch {
            _times.value = MainApp.repository.getHoraires(routeId, direction, date)
        }
    }

    fun loadTimesForDate(routeId: String, direction: String, date: String) {
        viewModelScope.launch {
            _times.value = MainApp.repository.getHoraires(routeId, direction, date)
        }
    }

}
