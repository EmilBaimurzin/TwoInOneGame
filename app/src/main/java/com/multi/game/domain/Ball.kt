package com.multi.game.domain

import com.multi.game.core.library.random
import java.util.Random

data class Ball(
    val color: Int = 1 random 5,
    var position: Pair<Float,Float>,
    var isFlying: Boolean,
    var verticalLine: Int = 1,
    var horizontalPosition: Int
)