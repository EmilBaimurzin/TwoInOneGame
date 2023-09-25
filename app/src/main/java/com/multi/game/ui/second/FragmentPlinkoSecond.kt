package com.multi.game.ui.second

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.multi.game.R
import com.multi.game.core.library.GameFragment
import com.multi.game.databinding.FragmentPlinkoSecondBinding
import com.multi.game.domain.SP
import com.multi.game.ui.first.FragmentAviaFirstDirections
import com.multi.game.ui.other.CallbackVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentPlinkoSecond :
    GameFragment<FragmentPlinkoSecondBinding>(FragmentPlinkoSecondBinding::inflate) {
    private val viewModel: PlinkoSecondViewModel by viewModels()
    private val callbackViewModel: CallbackVM by activityViewModels()
    private val sp by lazy {
        SP(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(20)
            viewModel.initCannon((dpToPx(60) + dpToPx(15)).toFloat(), binding.field.y)
            if (viewModel.gameState && !viewModel.pauseState) {
                viewModel.start(
                    binding.cannon.width,
                    binding.field.x + binding.field.width - dpToPx(60),
                    binding.field.x + dpToPx(60),
                    topLimit = (binding.topPlank.y).toInt(),
                    longLineStartedDotPosition = binding.bigRowX.x.toInt(),
                    shortLineStartedDotPosition = binding.smallRowX.x.toInt(),
                    horizontalDistanceBetweenDots = dpToPx(42),
                    verticaDistanceBetweenDots = dpToPx(37),
                    ballSize = dpToPx(25),
                )
            }
        }

        callbackViewModel.callback = {
            viewModel.pauseState = false
            viewModel.start(
                binding.cannon.width,
                binding.field.x + binding.field.width - dpToPx(60),
                binding.field.x + dpToPx(60),
                topLimit = (binding.topPlank.y).toInt(),
                longLineStartedDotPosition = binding.bigRowX.x.toInt(),
                shortLineStartedDotPosition = binding.smallRowX.x.toInt(),
                horizontalDistanceBetweenDots = dpToPx(42),
                verticaDistanceBetweenDots = dpToPx(37),
                ballSize = dpToPx(25),
            )
        }

        binding.menu.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.pause.setOnClickListener {
            viewModel.stop()
            viewModel.pauseState = true
            findNavController().navigate(R.id.action_fragmentPlinkoSecond_to_dialogPause)
        }

        binding.go.setOnClickListener {
            viewModel.spawnBall(binding.cannon.height, (binding.cannon.width - dpToPx(25)) / 2)
        }

        viewModel.cannonPosition.observe(viewLifecycleOwner) {
            binding.cannon.apply {
                x = it.first
                y = it.second
            }
        }

        viewModel.scores.observe(viewLifecycleOwner) {
            binding.score.text = it.toString()
        }

        viewModel.timer.observe(viewLifecycleOwner) { totalSecs ->
            val minutes = (totalSecs % 3600) / 60;
            val seconds = totalSecs % 60;

            binding.time.text = String.format("%02d:%02d", minutes, seconds);

            if (totalSecs == 0 && viewModel.gameState && !viewModel.pauseState) {
                viewModel.gameState = false
                viewModel.stop()
                if (sp.getBestScore(2) < viewModel.scores.value!!) {
                    sp.setBestScore(2, viewModel.scores.value!!)
                }
                findNavController().navigate(
                    FragmentPlinkoSecondDirections.actionFragmentPlinkoSecondToFragmentGameResult(
                        2,
                        viewModel.scores.value!!
                    )
                )
            }
        }

        viewModel.balls.observe(viewLifecycleOwner) {
            binding.ballsLayout.removeAllViews()
            it.forEach { ball ->
                val ballView = ImageView(requireContext())
                ballView.layoutParams = ViewGroup.LayoutParams(dpToPx(25), dpToPx(25))
                ballView.setImageResource(
                    when (ball.color) {
                        1 -> R.drawable.game02_ball01
                        2 -> R.drawable.game02_ball02
                        3 -> R.drawable.game02_ball03
                        4 -> R.drawable.game02_ball04
                        else -> R.drawable.game02_ball05
                    }
                )
                ballView.x = ball.position.first - dpToPx(7)
                ballView.y = ball.position.second
                binding.ballsLayout.addView(ballView)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}