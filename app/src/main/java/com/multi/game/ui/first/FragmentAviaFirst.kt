package com.multi.game.ui.first

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.multi.game.R
import com.multi.game.core.library.GameFragment
import com.multi.game.databinding.FragmentAviaFirstBinding
import com.multi.game.domain.SP
import com.multi.game.ui.other.CallbackVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentAviaFirst: GameFragment<FragmentAviaFirstBinding>(FragmentAviaFirstBinding::inflate) {
    private val viewModel: AviaFirstViewModel by viewModels()
    private val callbackViewModel: CallbackVM by activityViewModels()
    private val sp by lazy {
        SP(requireContext())
    }
    private var moveScope = CoroutineScope(Dispatchers.Default)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtons()

        callbackViewModel.callback = {
            viewModel.pauseState = false
            viewModel.start(
                dpToPx(120),
                dpToPx(80),
                xy.y.toInt(),
                xy.x.toInt(),
                binding.plane.width,
                binding.plane.height
            )
        }

        binding.menu.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.pause.setOnClickListener {
            viewModel.stop()
            viewModel.pauseState = true
            findNavController().navigate(R.id.action_fragmentAviaFirst_to_dialogPause)
        }

        viewModel.endCallback = {
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.stop()
                viewModel.gameState = false

                if (sp.getBestScore(1) < viewModel.score.value) {
                    sp.setBestScore(1, viewModel.score.value)
                }
                findNavController().navigate(
                    FragmentAviaFirstDirections.actionFragmentAviaFirstToFragmentGameResult(
                        1,
                        viewModel.score.value
                    )
                )
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.enemies.collect {
                    binding.otherLayout.removeAllViews()
                    it.forEach { enemy ->
                        val enemyView = ImageView(requireContext())
                        enemyView.layoutParams = ViewGroup.LayoutParams(dpToPx(120), dpToPx(80))
                        enemyView.setImageResource(if (enemy.value) R.drawable.game01_obstacle01 else R.drawable.game01_obstacle02)
                        enemyView.x = enemy.x
                        enemyView.y = enemy.y
                        binding.otherLayout.addView(enemyView)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playerXY.collect {
                    binding.plane.x = it.x
                    binding.plane.y = it.y
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.score.collect {
                    binding.score.text = it.toString()
                }
            }
        }

        lifecycleScope.launch {
            delay(10)
            if (viewModel.playerXY.value.y == 0f) {
                viewModel.setPlayerXY(
                    ((xy.x / 2) - (binding.plane.width / 2)),
                    xy.y - dpToPx(180).toFloat()
                )
            }
            if (viewModel.gameState && !viewModel.pauseState) {
                viewModel.start(
                    dpToPx(120),
                    dpToPx(80),
                    xy.y.toInt(),
                    xy.x.toInt(),
                    binding.plane.width,
                    binding.plane.height
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        binding.root.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            if (motionEvent.x > xy.x / 2) {
                                viewModel.playerMoveRight((xy.x - binding.plane.width))
                                delay(2)
                            } else {
                                viewModel.playerMoveLeft(0f)
                                delay(2)
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            if (motionEvent.x > xy.x / 2) {
                                viewModel.playerMoveRight((xy.x - binding.plane.width))
                                delay(2)
                            } else {
                                viewModel.playerMoveLeft(0f)
                                delay(2)
                            }
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
        moveScope.cancel()
    }
}