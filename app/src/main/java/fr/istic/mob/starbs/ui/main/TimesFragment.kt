package fr.istic.mob.starbs.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.databinding.FragmentTimesBinding
import kotlinx.coroutines.launch

class TimesFragment : Fragment() {

    private lateinit var binding: FragmentTimesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeId = requireArguments().getString(ARG_ROUTE_ID) ?: return
        val direction = requireArguments().getString(ARG_DIRECTION) ?: return
        val stopId = requireArguments().getString(ARG_STOP_ID) ?: return
        val date = requireArguments().getString(ARG_DATE) ?: return
        val afterTime = requireArguments().getString(ARG_AFTER_TIME) ?: "00:00:00"

        val normalizedAfterTime = when (afterTime.length) {
            5 -> "$afterTime:00"      // "HH:MM" -> "HH:MM:SS"
            8 -> afterTime            // déjà OK
            else -> "00:00:00"
        }

        binding.recyclerTimes.layoutManager = LinearLayoutManager(requireContext())

        Log.d("TIMES", "routeId: $routeId, direction: $direction, stopId: $stopId, afterTime: $normalizedAfterTime,date: $date")
        lifecycleScope.launch {
            // Il faut une méthode repository qui filtre par stopId + afterTime
            val times = MainApp.repository.getTimesFor(routeId, direction, stopId, afterTime)
            Log.d("TIMES", times.toString())

            binding.recyclerTimes.adapter = TimesAdapter(times) { clickedTime ->
                // TODO plus tard: ouvrir fragment 4 (détails jusqu’au terminus)
            }
        }


    }

    private fun normalizeDateToGtfs(input: String): String {
        // input: "31/12/2025" -> "20251231"
        return try {
            val parts = input.split("/")
            val dd = parts[0].padStart(2, '0')
            val mm = parts[1].padStart(2, '0')
            val yyyy = parts[2]
            "$yyyy$mm$dd"
        } catch (e: Exception) {
            input // si déjà au bon format
        }
    }

    companion object {
        const val ARG_ROUTE_ID = "routeId"
        const val ARG_DIRECTION = "direction"
        const val ARG_STOP_ID = "stopId"
        const val ARG_DATE = "date"
        const val ARG_AFTER_TIME = "afterTime"
    }
}
