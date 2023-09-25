package com.multi.game.ui.first

import androidx.lifecycle.ViewModel
import com.multi.game.core.library.XY
import com.multi.game.core.library.XYIMpl
import com.multi.game.core.library.random
import com.multi.game.domain.Enemy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Random

class AviaFirstViewModel : ViewModel() {
    var gameState = true
    var pauseState = false

    private var gameScope = CoroutineScope(Dispatchers.Default)

    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies = _enemies.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _playerXY = MutableStateFlow(XYIMpl(0f, 0f))
    val playerXY = _playerXY.asStateFlow()

    fun setPlayerXY(x: Float, y: Float) {
        _playerXY.value = XYIMpl(x, y)
    }

    private var isSpawning = false

    var endCallback: (() -> Unit)? = null

    fun stop() {
        gameScope.cancel()
    }

    fun start(
        enemyWidth: Int,
        enemyHeight: Int,
        maxY: Int,
        maxX: Int,
        playerWidth: Int,
        playerHeight: Int
    ) {
        gameScope = CoroutineScope(Dispatchers.Default)
        generateEnemies(enemyWidth, enemyHeight, maxX)
        letEnemiesMove(maxY,  enemyWidth, enemyHeight, playerWidth, playerHeight)
    }

    private fun generateEnemies(
        enemyWidth: Int,
        enemyHeight: Int,
        maxX: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(2000)
                isSpawning = true
                val newEnemies = _enemies.value.toMutableList()
                newEnemies.add(Enemy((0 random (maxX - enemyWidth)).toFloat(), 0f - enemyHeight, Random().nextBoolean()))
                _enemies.value = (newEnemies)
                isSpawning = false
            }
        }
    }

    private fun letEnemiesMove(
        maxY: Int,
        enemyWidth: Int,
        enemyHeight: Int,
        playerWidth: Int,
        playerHeight: Int,
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                if (!isSpawning) {
                    val currentList = _enemies.value
                    val newList = mutableListOf<Enemy>()
                    currentList.forEach { enemy ->
                        if (enemy.y <= maxY) {
                            val enemyX = enemy.x.toInt()..enemy.x.toInt() + enemyWidth
                            val enemyY = enemy.y.toInt()..enemy.y.toInt() + enemyHeight

                            val playerX = _playerXY.value.x.toInt().._playerXY.value.x.toInt() + playerWidth
                            val playerY = _playerXY.value.y.toInt().._playerXY.value.y.toInt() + playerHeight

                            if (playerX.any { it in enemyX } && playerY.any { it in enemyY }) {
                                endCallback?.invoke()
                            } else {
                                newList.add(enemy.copy(y = enemy.y + 7))
                            }
                        } else {
                            _score.value += 1
                        }
                    }
                    _enemies.value = (newList)
                }
            }
        }
    }

    fun playerMoveLeft(limit: Float) {
        if (_playerXY.value.x - 4f > limit) {
            _playerXY.value = (XYIMpl(_playerXY.value.x - 4, _playerXY.value.y))
        }
    }

    fun playerMoveRight(limit: Float) {
        if (_playerXY.value.x + 4f < limit) {
            _playerXY.value = (XYIMpl(_playerXY.value.x + 4, _playerXY.value.y))
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameScope.cancel()
    }
}