package com.kizitonwose.android.disposebag

import android.arch.lifecycle.Lifecycle

object DisposeBagPlugins {
    @JvmStatic var defaultLifecycleDisposeEvent = Lifecycle.Event.ON_DESTROY
}