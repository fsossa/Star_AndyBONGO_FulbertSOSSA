package fr.istic.mob.starbs.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.databinding.FragmentSearchStopsBinding
import fr.istic.mob.starbs.ui.search.StopRoutesFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchStopsFragment : Fragment() {

    private lateinit var binding: FragmentSearchStopsBinding
    private lateinit var adapter: SearchStopsAdapter
    private var searchJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchStopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerStops.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchStopsAdapter(emptyList()) { stop ->
            // Ouvrir StopRoutesFragment
            val frag = StopRoutesFragment().apply {
                arguments = Bundle().apply {
                    putString(StopRoutesFragment.ARG_STOP_NAME, stop.stop_name)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
                .replace(R.id.fragmentContainer, frag)
                .addToBackStack(null)
                .commit()
        }
        binding.recyclerStops.adapter = adapter

        binding.editQuery.addTextChangedListener { editable ->
            val q = editable?.toString()?.trim().orEmpty()

            searchJob?.cancel()
            if (q.length < 3) {
                adapter.submit(emptyList())
                binding.textInfo.visibility = View.VISIBLE
                binding.textInfo.text = "Entrez au moins 3 caractères"
                return@addTextChangedListener
            }

            // petit debounce (évite spam DB)
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(200)
                val results = MainApp.repository.searchStops(q)
                if (results.isEmpty()) {
                    adapter.submit(emptyList())
                    binding.textInfo.visibility = View.VISIBLE
                    binding.textInfo.text = "Aucun arrêt trouvé"
                } else {
                    binding.textInfo.visibility = View.GONE
                    adapter.submit(results)
                }
            }
        }
    }
}
