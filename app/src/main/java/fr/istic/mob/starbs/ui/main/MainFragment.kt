package fr.istic.mob.starbs.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.istic.mob.starbs.databinding.FragmentMainBinding
import fr.istic.mob.starbs.data.local.entities.Route

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel

    private var selectedRoute: Route? = null
    private var selectedDirection: String? = null
    private var selectedDate: String = "20240101"   // valeur par défaut


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setupObservers()
        setupListeners()

        viewModel.loadRoutes()

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
    }

    private fun setupListeners() {
        binding.spinnerRoutes.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val route = viewModel.routes.value?.get(position)

                    selectedDirection = null  // reset direction
                    binding.listDirections.adapter = null

                    selectedRoute?.let {
                        viewModel.loadDirections(it.route_id)
                    }
                    if (route != null) {
                        viewModel.loadDirections(route.route_id)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                    // rien à faire
                }
            }

        binding.listDirections.setOnItemClickListener { _, _, position, _ ->
            val direction = viewModel.directions.value?.get(position)
            val route = viewModel.routes.value?.get(binding.spinnerRoutes.selectedItemPosition)

            if (route != null && direction != null) {
                viewModel.loadTimes(route.route_id, direction, selectedDate)
            }
        }

        binding.datePicker.setOnDateChangedListener { _, year, month, day ->
            val date = "%04d%02d%02d".format(year, month + 1, day)

            val direction = selectedDirection
            val route = selectedRoute

            if (route != null && direction != null) {
                viewModel.loadTimesForDate(route.route_id, direction, date)
            }
        }

    }
}
