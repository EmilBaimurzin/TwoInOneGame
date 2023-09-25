package com.multi.game.ui.game_result

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.multi.game.R
import com.multi.game.core.library.GameFragment
import com.multi.game.databinding.FragmentGameResultBinding
import com.multi.game.domain.SP

class FragmentGameResult :
    GameFragment<FragmentGameResultBinding>(FragmentGameResultBinding::inflate) {
    private val args: FragmentGameResultArgs by navArgs()
    private val sp by lazy {
        SP(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.game1.isVisible = args.game == 1
        binding.game2.isVisible = args.game == 2
        binding.score.text = args.score.toString()
        binding.bestScore.text = sp.getBestScore(args.game).toString()

        binding.root.setBackgroundResource(if (args.game == 1) R.drawable.background04 else R.drawable.background07)

        binding.restart.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
            if (args.game == 1) {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentAviaFirst)
            } else {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentPlinkoSecond)
            }
        }
    }
}