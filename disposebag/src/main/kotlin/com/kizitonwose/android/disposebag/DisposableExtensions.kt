package com.kizitonwose.android.disposebag

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.disposables.Disposable

fun Disposable.disposedBy(bag: DisposeBag) = bag.add(this)

fun Disposable.disposedWith(owner: LifecycleOwner,
                            event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent) {

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

        private fun removeObserverAndDispose(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            dispose()
        }
    })
}