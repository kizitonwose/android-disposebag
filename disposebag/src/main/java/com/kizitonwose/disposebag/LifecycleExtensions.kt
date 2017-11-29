package com.kizitonwose.disposebag

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.disposables.Disposable

/**
 * Created by Kizito Nwose
 */

fun Disposable.disposedBy(owner: LifecycleOwner, event: Lifecycle.Event = Lifecycle.Event.ON_DESTROY) {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {

        override fun onPause(owner: LifecycleOwner) {
            if (event == Lifecycle.Event.ON_PAUSE) {
                removeObserverAndDispose(owner)
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            if (event == Lifecycle.Event.ON_STOP) {
                removeObserverAndDispose(owner)
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                removeObserverAndDispose(owner)
            }
        }

        fun removeObserverAndDispose(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            dispose()
        }
    })
}