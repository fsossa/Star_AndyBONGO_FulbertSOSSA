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
        val afterTime = requireArguments().getString(ARG_AFTER_TIME) ?: "00:00:00"

        Log.d("DEBUG_TIMES", "routeId=$routeId direction=$direction stopId=$stopId afterTime=$afterTime")

        binding.recyclerTimes.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            // ⚠️ Il faut une méthode repository qui filtre par stopId + afterTime
            val times = MainApp.repository.getTimesFor(routeId, direction, stopId, afterTime)

            binding.recyclerTimes.adapter = TimesAdapter(times) { clickedTime ->
                // TODO plus tard: ouvrir fragment 4 (détails jusqu’au terminus)
            }
        }
    }

    companion object {
        const val ARG_ROUTE_ID = "routeId"
        const val ARG_DIRECTION = "direction"
        const val ARG_STOP_ID = "stopId"
        const val ARG_AFTER_TIME = "afterTime"
    }
}
