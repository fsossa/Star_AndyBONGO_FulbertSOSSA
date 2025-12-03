package fr.istic.mob.starbs.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.databinding.FragmentMainBinding
import fr.istic.mob.starbs.ui.components.setOnItemSelectedListener
import kotlinx.coroutines.launch
import java.util.*

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.recyclerDirections.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        setupTimePicker()
        loadRoutes()     // <---- async
    }

    private fun setupDatePicker() {
        binding.buttonSelectDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    binding.buttonSelectDate.text = "$d/${m + 1}/$y"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker() {
        binding.buttonSelectTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, min ->
                    val h = hour.toString().padStart(2, '0')
                    val m = min.toString().padStart(2, '0')
                    binding.buttonSelectTime.text = "$h:$m"
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    // -------------------------------------------------------
    // Charger les lignes de bus (appel Repository suspend)
    // -------------------------------------------------------
    private fun loadRoutes() {
        val repo = MainApp.repository

        viewLifecycleOwner.lifecycleScope.launch {
            val routes = repo.getAllRoutes()

            val adapter = RouteSpinnerAdapter(requireContext(), routes)
            binding.spinnerRoutes.adapter = adapter

            // Quand une ligne est choisie, on chargera ses directions
            binding.spinnerRoutes.setOnItemSelectedListener { _, _, position, _ ->
                val route = routes[position]
                loadDirections(route.route_id)
            }
        }
    }

    // -------------------------------------------------------
    // Charger les directions de la ligne sélectionnée
    // -------------------------------------------------------
    private fun loadDirections(routeId: String) {
        val repo = MainApp.repository

        viewLifecycleOwner.lifecycleScope.launch {
            val directions = repo.getDirectionsForRoute(routeId)

            val adapter = DirectionRecyclerAdapter(directions) { selectedDirection ->
                // TODO : ouvrir l’écran des horaires pour cette direction
                println("Direction sélectionnée : $selectedDirection")
            }

            binding.recyclerDirections.adapter = adapter
        }
    }

}
