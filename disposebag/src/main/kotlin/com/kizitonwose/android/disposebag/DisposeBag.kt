package com.kizitonwose.android.disposebag

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

/**
 * Created by Kizito Nwose
 */

class DisposeBag @JvmOverloads constructor(owner: LifecycleOwner,
                                           private val event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent)
    : Disposable, DisposableContainer, DefaultLifecycleObserver {

    private val lifecycle = owner.lifecycle

    private val composite by lazy { CompositeDisposable() }

    @JvmOverloads constructor(resources: Iterable<Disposable>,
                              owner: LifecycleOwner,
                              event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent)
            : this(owner, event) {

        resources.forEach { composite.add(it) }
    }

    init {
        lifecycle.addObserver(this)
    }

    override fun isDisposed() = composite.isDisposed

    override fun dispose() {
        lifecycle.removeObserver(this)
        composite.dispose()
    }

    override fun add(disposable: Disposable) = composite.add(disposable)

    override fun remove(disposable: Disposable) = composite.remove(disposable)

    override fun delete(disposable: Disposable) = composite.delete(disposable)

    override fun onPause(owner: LifecycleOwner) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            dispose()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (event == Lifecycle.Event.ON_STOP) {
            dispose()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            dispose()
        }
    }
}

