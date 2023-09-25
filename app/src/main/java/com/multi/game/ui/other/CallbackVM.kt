package com.multi.game.ui.other

import androidx.lifecycle.ViewModel

class CallbackVM: ViewModel() {
    var callback: (() -> Unit)? = null
}