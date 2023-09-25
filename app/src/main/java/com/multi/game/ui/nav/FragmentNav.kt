package com.multi.game.ui.nav

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.multi.game.R
import com.multi.game.core.library.GameFragment
import com.multi.game.databinding.FragmentNavBinding

class FragmentNav : GameFragment<FragmentNavBinding>(FragmentNavBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            game1.setOnClickListener {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentAviaFirst)
            }
            game2.setOnClickListener {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentPlinkoSecond)
            }
            exit.setOnClickListener {
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.fragmentMain, false, false)
            }
        })

        binding.privacyText.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }
}