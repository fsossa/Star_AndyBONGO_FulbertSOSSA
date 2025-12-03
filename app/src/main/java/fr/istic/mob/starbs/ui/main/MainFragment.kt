package fr.istic.mob.starbs.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.istic.mob.starbs.data.local.entities.Route
import fr.istic.mob.starbs.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel

    private var selectedRoute: Route? = null
    private var selectedDirection: String? = null
    private var selectedDate: String = "20240101"

    private var selectedTime: String = "00:00"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.routes.observe(viewLifecycleOwner) { list ->
            val adapter = RouteSpinnerAdapter(requireContext(), list)
            binding.spinnerRoutes.adapter = adapter
        }

        viewModel.directions.observe(viewLifecycleOwner) { dirs ->
            binding.listDirections.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dirs)
        }

        viewModel.times.observe(viewLifecycleOwner) { times ->
            binding.listTimes.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, times)
        }

//        viewModel.progress.observe(viewLifecycleOwner) { (percent, msg) ->
//            if (percent in 1..99) {
//                binding.progressText.visibility = View.VISIBLE
//                binding.progressText.text = "$msg\n$percent %"
//                binding.progressCircle.visibility = View.VISIBLE
//                binding.progressCircle.progress = percent
//
//                // On désactive le reste pendant le remplissage
//                setMainUiEnabled(false)
//            } else {
//                binding.progressText.visibility = View.GONE
//                binding.progressCircle.visibility = View.GONE
//                setMainUiEnabled(true)
//            }
//        }
    }

    private fun setMainUiEnabled(enabled: Boolean) {
        binding.buttonChooseDate.isEnabled = enabled
        binding.buttonChooseTime.isEnabled = enabled
        binding.spinnerRoutes.isEnabled = enabled
        binding.listDirections.isEnabled = enabled
        binding.listTimes.isEnabled = enabled
    }

    private fun setupListeners() {
        // Date
        binding.buttonChooseDate.setOnClickListener {
            val now = java.util.Calendar.getInstance()
            val dlg = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    selectedDate = "%04d%02d%02d".format(y, m + 1, d)
                    binding.textSelectedDate.text =
                        "Date sélectionnée : %02d/%02d/%04d".format(d, m + 1, y)
                },
                now.get(java.util.Calendar.YEAR),
                now.get(java.util.Calendar.MONTH),
                now.get(java.util.Calendar.DAY_OF_MONTH)
            )
            dlg.show()
        }

        // Heure
        binding.buttonChooseTime.setOnClickListener {
            val now = java.util.Calendar.getInstance()
            val dlg = TimePickerDialog(
                requireContext(),
                { _, h, min ->
                    selectedTime = "%02d:%02d".format(h, min)
                    binding.textSelectedTime.text =
                        "Heure choisie : %02d:%02d".format(h, min)
                },
                now.get(java.util.Calendar.HOUR_OF_DAY),
                now.get(java.util.Calendar.MINUTE),
                true
            )
            dlg.show()
        }

        // Spinner routes
        binding.spinnerRoutes.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val route = viewModel.routes.value?.get(position)
                    selectedRoute = route
                    selectedDirection = null
                    if (route != null) {
                        viewModel.loadDirections(route.route_id)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }

        // Liste directions
        binding.listDirections.setOnItemClickListener { _, _, position, _ ->
            val direction = viewModel.directions.value?.get(position)
            val route = selectedRoute
            selectedDirection = direction
            if (route != null && direction != null) {
                viewModel.loadTimes(route.route_id, direction, selectedDate, selectedTime)
            }
        }
    }
}
