package fr.istic.mob.starbs.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.databinding.FragmentPassagesBinding
import kotlinx.coroutines.launch

class PassagesFragment : Fragment() {

    private lateinit var binding: FragmentPassagesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPassagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeId = requireArguments().getString(ARG_ROUTE_ID) ?: return
        val direction = requireArguments().getString(ARG_DIRECTION) ?: return
        val stopId = requireArguments().getString(ARG_STOP_ID) ?: return
        val clickedTime = requireArguments().getString(ARG_CLICKED_TIME) ?: return

        binding.recyclerPassages.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val rows = MainApp.repository.getPassagesToTerminus(routeId, direction, stopId, clickedTime)
            binding.recyclerPassages.adapter = PassagesAdapter(rows)
        }
    }

    companion object {
        const val ARG_ROUTE_ID = "routeId"
        const val ARG_DIRECTION = "direction"
        const val ARG_STOP_ID = "stopId"
        const val ARG_CLICKED_TIME = "clickedTime"
    }
}
