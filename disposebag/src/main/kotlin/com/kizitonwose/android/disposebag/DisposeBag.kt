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

fun Disposable.disposedBy(bag: DisposeBag) = bag.add(this)

object DisposeBagPlugins  {
    @JvmStatic var defaultLifecycleDisposeEvent = Lifecycle.Event.ON_DESTROY
}

class DisposeBag @JvmOverloads constructor(private val owner: LifecycleOwner,
                                           private val event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent)
    : Disposable, DisposableContainer, DefaultLifecycleObserver {

    @JvmOverloads constructor(resources: Iterable<Disposable>,
                              owner: LifecycleOwner,
                              event: Lifecycle.Event = DisposeBagPlugins.defaultLifecycleDisposeEvent)
            : this(owner = owner, event = event) {

        resources.forEach { composite.add(it) }
    }

    // We could extend CompositeDisposable but the class is final
    private val composite by lazy { CompositeDisposable() }

    init {
        owner.lifecycle.addObserver(this)
    }

    override fun isDisposed() = composite.isDisposed

    override fun dispose() = composite.dispose()

    override fun add(d: Disposable) = composite.add(d)

    override fun remove(d: Disposable) = composite.remove(d)

    override fun delete(d: Disposable) = composite.delete(d)

    fun clear() = composite.clear()

    override fun onPause(owner: LifecycleOwner) {
        if (event == Lifecycle.Event.ON_PAUSE) dispose()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (event == Lifecycle.Event.ON_STOP) dispose()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (event == Lifecycle.Event.ON_DESTROY) dispose()
    }
}

