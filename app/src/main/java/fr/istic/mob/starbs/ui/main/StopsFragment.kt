package fr.istic.mob.starbs.ui.main
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.databinding.FragmentStopsBinding
import kotlinx.coroutines.launch

class StopsFragment : Fragment() {

    private lateinit var binding: FragmentStopsBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeId = requireArguments().getString("routeId")!!
        val direction = requireArguments().getString("direction")!!
        Log.d("DEBUG_STOPS", "routeId=${routeId}, direction=${direction}")


        lifecycleScope.launch {
            val stops = MainApp.repository.getStopsFor(routeId, direction)
            Log.d("DEBUG_STOPS", "stops=${stops}")
            binding.recyclerStops.layoutManager = LinearLayoutManager(requireContext())

            binding.recyclerStops.adapter = StopsAdapter(stops) { stop ->
                // TODO: ouvrir le fragment horaires
            }
        }
    }
}
