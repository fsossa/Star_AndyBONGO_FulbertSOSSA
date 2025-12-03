package fr.istic.mob.starbs.ui.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.databinding.FragmentLoadingBinding
import fr.istic.mob.starbs.ui.main.MainViewModel

class LoadingFragment : Fragment() {

    private lateinit var binding: FragmentLoadingBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        observeProgress()

        return binding.root
    }

    private fun observeProgress() {
        viewModel.progress.observe(viewLifecycleOwner) { (percent, message) ->

            binding.progressText.text = "$message\n$percent %"
            binding.progressCircle.progress = percent

            if (percent == 0 && message.contains("Erreur", ignoreCase = true)) {
                binding.buttonRetry.visibility = View.VISIBLE
            }

            if (percent >= 100) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fr.istic.mob.starbs.ui.main.MainFragment())
                    .commit()
            }
        }

        binding.buttonRetry.setOnClickListener {
            viewModel.downloadGTFS(requireContext())
        }
    }
}
