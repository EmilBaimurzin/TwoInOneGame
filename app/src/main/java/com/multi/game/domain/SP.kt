package com.multi.game.domain

import android.content.Context

class SP(private val context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getBestScore(game: Int): Int = sp.getInt(game.toString(), 0)

    fun setBestScore(game: Int, score: Int) = sp.edit().putInt(game.toString(), score).apply()
}