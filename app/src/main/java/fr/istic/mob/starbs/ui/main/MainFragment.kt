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
import kotlinx.coroutines.delay
import java.util.Calendar

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel

    private var selectedRoute: Route? = null
    private var selectedDirection: String? = null

    private var selectedDate: String = ""   // YYYYMMDD
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        initDefaultDate()
        setupObservers()
        setupListeners()

        viewModel.loadRoutes()

        return binding.root
    }

    private fun initDefaultDate() {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)

        selectedDate = "%04d%02d%02d".format(y, m, d)
        binding.textSelectedDate.text = "Date sélectionnée : %02d/%02d/%04d".format(d, m, y)
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

        viewModel.progress.observe(viewLifecycleOwner) { (percent, msg) ->
            if (percent in 0..99) {
                binding.progressBar.visibility = View.VISIBLE
                binding.progressText.visibility = View.VISIBLE
                binding.progressBar.progress = percent
                binding.progressText.text = "$percent% - $msg"
                binding.progressCircle.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.GONE
                binding.progressCircle.visibility = View.GONE
            }
            if (percent >= 100) {
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.VISIBLE
                binding.progressText.text = "GTFS chargé ✔"
                binding.progressCircle.visibility = View.GONE
                binding.progressText.visibility = View.GONE
            }
        }

        viewModel.progressMessage.observe(viewLifecycleOwner) { msg ->
            binding.progressText.text = msg
        }
    }

    private fun setupListeners() {

        binding.buttonChooseDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate = "%04d%02d%02d".format(year, month + 1, day)
                    binding.textSelectedDate.text =
                        "Date sélectionnée : %02d/%02d/%04d".format(day, month + 1, year)
                    reloadTimesIfPossible()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }

        binding.buttonChooseTime.setOnClickListener {
            val dialog = TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    selectedHour = h
                    selectedMinute = m
                    binding.textSelectedTime.text = "Heure choisie : %02d:%02d".format(h, m)
                    reloadTimesIfPossible()
                },
                selectedHour,
                selectedMinute,
                true
            )
            dialog.show()
        }

        binding.spinnerRoutes.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedRoute = viewModel.routes.value?.get(position)
                    selectedDirection = null
                    binding.listDirections.adapter = null
                    binding.listTimes.adapter = null
                    selectedRoute?.let {
                        viewModel.loadDirections(it.route_id)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }

        binding.listDirections.setOnItemClickListener { _, _, position, _ ->
            selectedDirection = viewModel.directions.value?.get(position)
            reloadTimesIfPossible()
        }
    }

    private fun reloadTimesIfPossible() {
        val route = selectedRoute ?: return
        val direction = selectedDirection ?: return
        val timeStr = "%02d:%02d:00".format(selectedHour, selectedMinute)
        viewModel.loadTimes(route.route_id, direction, selectedDate, timeStr)
    }
}
