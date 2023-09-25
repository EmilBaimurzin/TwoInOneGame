package com.multi.game.domain

import com.multi.game.core.library.XY

data class Enemy (
    override var x: Float,
    override var y: Float,
    val value: Boolean
): XY