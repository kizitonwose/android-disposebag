package com.kizitonwose.android.disposebag

import android.arch.lifecycle.Lifecycle
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by Kizito Nwose
 */

class DisposeBagTest {

    private lateinit var lifecycleOwner: TestLifecycleOwner
    private lateinit var testScheduler: TestScheduler

    @Before
    fun before() {
        lifecycleOwner = TestLifecycleOwner()
        testScheduler = TestScheduler()
        lifecycleOwner.lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }


    @After
    fun after() {
        DisposeBagPlugins.defaultLifecycleDisposeEvent = Lifecycle.Event.ON_DESTROY
    }

    @Test
    fun disposedByDisposeBag() {
        val disposeBag = DisposeBag(lifecycleOwner)

        val emittedItems = mutableListOf<Long>()

        val disposable = Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    emittedItems.add(it)
                    System.out.println(it)
                }.apply { disposedBy(disposeBag) }

        assertTrue(emittedItems.isEmpty())

        testScheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        assertTrue(emittedItems.size == 4)

        lifecycleOwner.performDestroy()

        assertTrue(disposable.isDisposed)

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        assertTrue(emittedItems.size == 4)
    }

    @Test
    fun disposeMultipleByDisposeBagWithCustomEvent() {
        lifecycleOwner.lifecycleRegistry.markState(Lifecycle.State.STARTED)

        val disposable1 = Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    System.out.println(it)
                }

        val disposable2 = Observable.fromIterable(1..100)
                .delay(10, TimeUnit.SECONDS)
                .subscribe {
                    System.out.println(it)
                }

        val disposeBag = DisposeBag(listOf(disposable1, disposable2),
                lifecycleOwner, Lifecycle.Event.ON_STOP)

        assertFalse(disposable1.isDisposed)
        assertFalse(disposable2.isDisposed)
        assertFalse(disposeBag.isDisposed)

        lifecycleOwner.performStop()

        assertTrue(disposable1.isDisposed)
        assertTrue(disposable2.isDisposed)
        assertTrue(disposeBag.isDisposed)
    }

    @Test
    fun disposedWithLifecycle() {
        val emittedItems = mutableListOf<Long>()

        Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    emittedItems.add(it)
                    System.out.println(it)
                }.disposedWith(lifecycleOwner)

        assertTrue(emittedItems.isEmpty())

        testScheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        assertTrue(emittedItems.size == 4)

        lifecycleOwner.performDestroy()

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        assertTrue(emittedItems.size == 4)
    }

    @Test
    fun disposedWithLifecycleCustomEvent() {
        testLifecycleWithCustomEvent(Lifecycle.State.CREATED, Lifecycle.Event.ON_DESTROY)
        testLifecycleWithCustomEvent(Lifecycle.State.RESUMED, Lifecycle.Event.ON_PAUSE)
        testLifecycleWithCustomEvent(Lifecycle.State.STARTED, Lifecycle.Event.ON_STOP)
    }

    private fun testLifecycleWithCustomEvent(beginState: Lifecycle.State, endEvent: Lifecycle.Event) {
        lifecycleOwner.lifecycleRegistry.markState(beginState)

        val disposable = Observable.fromIterable(1..100)
                .delay(10, TimeUnit.SECONDS)
                .subscribe {
                    System.out.println(it)
                }.apply { disposedWith(lifecycleOwner, endEvent) }

        assertFalse(disposable.isDisposed)

        lifecycleOwner.performEvent(endEvent)

        assertTrue(disposable.isDisposed)
    }

    @Test
    fun overrideGlobalEvent(){
        DisposeBagPlugins.defaultLifecycleDisposeEvent = Lifecycle.Event.ON_PAUSE

        lifecycleOwner.lifecycleRegistry.markState(Lifecycle.State.RESUMED)

        val disposeBag = DisposeBag(lifecycleOwner)

        val disposable = Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    System.out.println(it)
                }.apply { disposedBy(disposeBag) }

        assertFalse(disposable.isDisposed)
        assertFalse(disposeBag.isDisposed)

        lifecycleOwner.performPause()

        assertTrue(disposable.isDisposed)
        assertTrue(disposeBag.isDisposed)
    }

}
