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
import fr.istic.mob.starbs.R
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


        lifecycleScope.launch {
            val stops = MainApp.repository.getStopsFor(routeId, direction)
            binding.recyclerStops.layoutManager = LinearLayoutManager(requireContext())

            binding.recyclerStops.adapter = StopsAdapter(stops) { stop ->
                val selectedDate = viewModel.selectedDate.value ?: ""
                val selectedTime = viewModel.selectedTime.value ?: ""

                val fragment = TimesFragment().apply {
                    arguments = Bundle().apply {
                        putString("routeId", routeId)
                        putString("direction", direction)
                        putString("stopId", stop.stop_id)
                        putString("date", selectedDate)
                        putString("afterTime", selectedTime)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.enter_from_right,  // fragment entrant (forward)
                        R.anim.exit_to_left,      // fragment sortant (forward)
                        R.anim.enter_from_left,   // fragment entrant (back)
                        R.anim.exit_to_right      // fragment sortant (back)
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
