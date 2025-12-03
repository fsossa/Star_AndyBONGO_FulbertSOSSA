package fr.istic.mob.starbs.ui.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fr.istic.mob.starbs.databinding.FragmentLoadingBinding
import fr.istic.mob.starbs.ui.main.MainViewModel

class LoadingFragment : Fragment() {

    private lateinit var binding: FragmentLoadingBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.progressPercent.observe(viewLifecycleOwner) {
            binding.progressCircular.progress = it
            binding.textProgress.text = "$it%"
        }

        viewModel.progressMessage.observe(viewLifecycleOwner) {
            binding.textLoadingSubtitle.text = it
        }
    }
}
