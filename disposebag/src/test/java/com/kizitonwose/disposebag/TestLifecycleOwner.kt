package com.kizitonwose.disposebag

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry


/**
 * Created by Kizito Nwose
 */

class TestLifecycleOwner : LifecycleOwner {

    val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle() = lifecycleRegistry

    fun performEvent(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)

    fun performPause() = performEvent(Lifecycle.Event.ON_PAUSE)

    fun performStop() = performEvent(Lifecycle.Event.ON_STOP)

    fun performDestroy() = performEvent(Lifecycle.Event.ON_DESTROY)
}
