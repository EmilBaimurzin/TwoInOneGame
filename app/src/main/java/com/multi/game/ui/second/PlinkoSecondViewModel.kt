package com.multi.game.ui.second

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.multi.game.core.library.random
import com.multi.game.domain.Ball
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PlinkoSecondViewModel: ViewModel() {
    var gameState = true
    var pauseState = false
    private var gameScope = CoroutineScope(Dispatchers.Default)
    private var cannonScopeRight = CoroutineScope(Dispatchers.Default)
    private var cannonScopeLeft = CoroutineScope(Dispatchers.Default)
    private var isCannonMovingRight = true

    private val _cannonPosition = MutableLiveData(0f to 0f)
    val cannonPosition: LiveData<Pair<Float, Float>> = _cannonPosition

    private val _balls = MutableLiveData<List<Ball>>(emptyList())
    val balls: LiveData<List<Ball>> = _balls

    private val _scores = MutableLiveData(0)
    val scores: LiveData<Int> = _scores

    private val _timer = MutableLiveData(60)
    val timer: LiveData<Int> = _timer

    fun start(
        cannonWidth: Int, maxX: Float, minX: Float, topLimit: Int,
        longLineStartedDotPosition: Int,
        shortLineStartedDotPosition: Int,
        horizontalDistanceBetweenDots: Int,
        verticaDistanceBetweenDots: Int,
        ballSize: Int
    ) {
        gameScope = CoroutineScope(Dispatchers.Default)
        if (isCannonMovingRight) {
            moveCannonRight(cannonWidth, maxX, minX)
        } else {
            moveCannonLeft(cannonWidth, maxX, minX)
        }
        startTimer()
        letBallsFall(
            topLimit,
            longLineStartedDotPosition,
            shortLineStartedDotPosition,
            horizontalDistanceBetweenDots,
            verticaDistanceBetweenDots,
            minX.toInt(),
            ballSize
        )
    }

    fun startTimer() {
        gameScope.launch {
            while (true) {
                delay(1000)
                _timer.postValue(_timer.value!! - 1)
            }
        }
    }

    fun stop() {
        gameScope.cancel()
        cannonScopeLeft.cancel()
        cannonScopeRight.cancel()
    }

    private fun moveCannonRight(cannonWidth: Int, maxX: Float, minX: Float) {
        cannonScopeRight = CoroutineScope(Dispatchers.Default)
        cannonScopeRight.launch {
            while (true) {
                delay(16)
                if (_cannonPosition.value!!.first + 5 + cannonWidth > maxX) {
                    moveCannonLeft(cannonWidth, maxX, minX)
                    isCannonMovingRight = false
                    cannonScopeRight.cancel()
                } else {
                    _cannonPosition.postValue(_cannonPosition.value!!.first + 5 to _cannonPosition.value!!.second)
                }
            }
        }
    }

    private fun moveCannonLeft(cannonWidth: Int, maxX: Float, minX: Float) {
        cannonScopeLeft = CoroutineScope(Dispatchers.Default)
        cannonScopeLeft.launch {
            while (true) {
                delay(16)
                if (_cannonPosition.value!!.first - 5 < minX) {
                    moveCannonRight(cannonWidth, maxX, minX)
                    isCannonMovingRight = true
                    cannonScopeLeft.cancel()
                } else {
                    _cannonPosition.postValue(_cannonPosition.value!!.first - 5 to _cannonPosition.value!!.second)
                }
            }
        }
    }

    fun initCannon(x: Float, y: Float) {
        if (_cannonPosition.value!! == 0f to 0f) {
            _cannonPosition.postValue(x to y)
        }
    }

    fun spawnBall(cannonHeight: Int, padding: Int) {
        viewModelScope.launch {
            if (_balls.value!!.isEmpty()) {
                val ball = Ball(
                    position = _cannonPosition.value!!.first + padding to _cannonPosition.value!!.second + cannonHeight,
                    isFlying = true,
                    horizontalPosition = 1
                )
                val newList = _balls.value!!.toMutableList()
                newList.add(ball)
                _balls.postValue(newList)
            }
        }
    }

    private fun letBallsFall(
        topLimit: Int,
        longLineStartedDotPosition: Int,
        shortLineStartedDotPosition: Int,
        horizontalDistanceBetweenDots: Int,
        verticaDistanceBetweenDots: Int,
        minX: Int,
        ballSize: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(300)
                val currentList = _balls.value!!
                val newList = mutableListOf<Ball>()
                currentList.forEach { ball ->
                    if (ball.isFlying) {
                        if (ball.position.second + 100 + ballSize <= topLimit) {
                            ball.position = ball.position.first to ball.position.second + 100
                        } else {
                            ball.isFlying = false
                            val ballHorizontalPosition = when (ball.position.first.toInt()) {
                                in minX..shortLineStartedDotPosition -> 1
                                in shortLineStartedDotPosition..shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 1) -> 2
                                in shortLineStartedDotPosition..shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 2) -> 3
                                in shortLineStartedDotPosition..shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 3) -> 4
                                in shortLineStartedDotPosition..shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 4) -> 5
                                in shortLineStartedDotPosition..shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 5) -> 6
                                else -> 7
                            }
                            ball.horizontalPosition = ballHorizontalPosition
                            val ballX = when (ball.horizontalPosition) {
                                1 -> shortLineStartedDotPosition
                                2 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                3 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                4 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                5 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                6 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                else -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                            }
                            ball.position = ballX.toFloat() to (topLimit - ballSize).toFloat()
                        }
                        newList.add(ball)
                    } else {
                        when (ball.verticalLine) {
                            1 -> {
                                val ballX = when (ball.horizontalPosition) {
                                    1 -> longLineStartedDotPosition
                                    2 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                    3 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                    4 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                    5 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                    6 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                    7 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                                    else -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 7)
                                }
                                ball.position =
                                    ballX.toFloat() to (topLimit - ballSize).toFloat() + verticaDistanceBetweenDots * 1
                                ball.verticalLine = 2
                                when (ball.horizontalPosition) {
                                    1 -> {
                                        ball.horizontalPosition = 1
                                    }

                                    8 -> {
                                        ball.horizontalPosition = 7
                                    }

                                    else -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition - 1 else ball.horizontalPosition
                                    }
                                }
                                newList.add(ball)
                            }

                            2 -> {
                                val ballX = when (ball.horizontalPosition) {
                                    1 -> shortLineStartedDotPosition
                                    2 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                    3 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                    4 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                    5 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                    6 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                    else -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                                }
                                ball.position =
                                    ballX.toFloat() to (topLimit - ballSize).toFloat() + verticaDistanceBetweenDots * 2
                                ball.verticalLine = 3
                                when (ball.horizontalPosition) {
                                    1 -> {
                                        if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }

                                    7 -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }

                                    else -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }
                                }
                                newList.add(ball)
                            }

                            3 -> {
                                val ballX = when (ball.horizontalPosition) {
                                    1 -> longLineStartedDotPosition
                                    2 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                    3 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                    4 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                    5 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                    6 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                    7 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                                    else -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 7)
                                }
                                ball.position =
                                    ballX.toFloat() to (topLimit - ballSize).toFloat() + verticaDistanceBetweenDots * 3
                                ball.verticalLine = 4
                                when (ball.horizontalPosition) {
                                    1 -> {
                                        ball.horizontalPosition = 1
                                    }

                                    8 -> {
                                        ball.horizontalPosition = 7
                                    }

                                    else -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition - 1 else ball.horizontalPosition
                                    }
                                }
                                newList.add(ball)
                            }

                            4 -> {
                                val ballX = when (ball.horizontalPosition) {
                                    1 -> shortLineStartedDotPosition
                                    2 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                    3 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                    4 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                    5 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                    6 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                    else -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                                }
                                ball.position =
                                    ballX.toFloat() to (topLimit - ballSize).toFloat() + verticaDistanceBetweenDots * 4
                                ball.verticalLine = 5
                                when (ball.horizontalPosition) {
                                    1 -> {
                                        if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }

                                    7 -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }

                                    else -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }
                                }
                                newList.add(ball)
                            }

                            5 -> {
                                val ballX = when (ball.horizontalPosition) {
                                    1 -> longLineStartedDotPosition
                                    2 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                    3 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                    4 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                    5 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                    6 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                    7 -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                                    else -> longLineStartedDotPosition + (horizontalDistanceBetweenDots * 7)
                                }
                                ball.position =
                                    ballX.toFloat() to (topLimit - ballSize).toFloat() + verticaDistanceBetweenDots * 5
                                ball.verticalLine = 6
                                when (ball.horizontalPosition) {
                                    1 -> {
                                        ball.horizontalPosition = 1
                                    }

                                    8 -> {
                                        ball.horizontalPosition = 7
                                    }

                                    else -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition - 1 else ball.horizontalPosition
                                    }
                                }
                                newList.add(ball)
                            }

                            6 -> {
                                val ballX = when (ball.horizontalPosition) {
                                    1 -> shortLineStartedDotPosition
                                    2 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 1)
                                    3 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 2)
                                    4 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 3)
                                    5 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 4)
                                    6 -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 5)
                                    else -> shortLineStartedDotPosition + (horizontalDistanceBetweenDots * 6)
                                }
                                ball.position =
                                    ballX.toFloat() to (topLimit - ballSize).toFloat() + verticaDistanceBetweenDots * 6
                                ball.verticalLine = 7
                                when (ball.horizontalPosition) {
                                    1 -> {
                                        if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }

                                    7 -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }

                                    else -> {
                                        ball.horizontalPosition =
                                            if (Random.nextBoolean()) ball.horizontalPosition else ball.horizontalPosition + 1
                                    }
                                }
                                newList.add(ball)
                            }

                            else -> {
                                val score = when (ball.horizontalPosition) {
                                    1 -> 20
                                    2 -> 50
                                    3 -> 10
                                    4 -> 50
                                    5 -> 100
                                    6 -> 0
                                    7 -> 20
                                    else -> 10
                                }
                                _scores.postValue(_scores.value!! + score)
                            }
                        }
                    }
                }
                _balls.postValue(newList)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}