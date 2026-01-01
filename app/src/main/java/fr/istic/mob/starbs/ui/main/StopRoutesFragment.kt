package fr.istic.mob.starbs.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.databinding.FragmentStopRoutesBinding
import kotlinx.coroutines.launch

class StopRoutesFragment : Fragment() {

    private lateinit var binding: FragmentStopRoutesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stopName = requireArguments().getString(ARG_STOP_NAME) ?: return
        binding.titleStopRoutes.text = stopName

        binding.recyclerStopRoutes.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val rows = MainApp.repository.getRoutesForStopName(stopName)

            // Group by route
            val grouped = rows.groupBy { it.route_id }.map { (_, list) ->
                RouteWithDirectionsUi(
                    routeId = list.first().route_id,
                    shortName = list.first().route_short_name,
                    longName = list.first().route_long_name,
                    color = list.first().route_color,
                    textColor = list.first().route_text_color,
                    directions = list.mapNotNull { it.trip_headsign }.distinct()
                )
            }.sortedBy { it.shortName }

            binding.recyclerStopRoutes.adapter =
                StopRoutesAdapter(grouped) { routeId, direction ->
                    // Ici: à toi de décider quoi faire au clic d'une direction
                    // Option A (simple): afficher un toast
                    // Option B: ouvrir le flux (StopsFragment / TimesFragment etc.)
                }

            // Message si vide
            binding.infoStopRoutes.visibility = if (grouped.isEmpty()) View.VISIBLE else View.GONE
            binding.infoStopRoutes.text = if (grouped.isEmpty()) "Aucune ligne trouvée." else ""
        }
    }

    companion object {
        const val ARG_STOP_NAME = "stopName"
    }
}
