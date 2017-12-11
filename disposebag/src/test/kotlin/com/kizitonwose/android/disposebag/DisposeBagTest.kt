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

    private lateinit var disposeBag: DisposeBag

    @Before
    fun before() {
        lifecycleOwner = TestLifecycleOwner()
        testScheduler = TestScheduler()
        lifecycleOwner.lifecycleRegistry.markState(Lifecycle.State.CREATED)
        disposeBag = DisposeBag(lifecycleOwner)
    }


    @After
    fun after() {
        DisposeBagPlugins.defaultLifecycleDisposeEvent = Lifecycle.Event.ON_DESTROY
    }

    @Test
    fun shouldDisposeOnDefaultEndEvent() {
        //Arrange
        val emittedItems = mutableListOf<Long>()

        //Act
        val disposable = Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    emittedItems.add(it)
                }.apply { disposedBy(disposeBag) }

        //Assert
        assertEmptyList(emittedItems)

        testScheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        assertListHasSize(emittedItems, 4)

        lifecycleOwner.performDestroy()

        assertTrue(disposable.isDisposed)

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        assertListHasSize(emittedItems, 4)
    }

    @Test
    fun shouldDisposeAllDisposablesWhenEndEventOccurs() {
        //Arrange
        lifecycleOwner.lifecycleRegistry.markState(Lifecycle.State.STARTED)
        val emmittedItems = mutableListOf<Long>()

        //Act
        val disposable1 = Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    emmittedItems.add(it)
                }

        val disposable2 = Observable.fromIterable(1..3)
                .delay(10, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    emmittedItems.add(it.toLong())
                }

        disposeBag = DisposeBag(listOf(disposable1, disposable2),
                lifecycleOwner, Lifecycle.Event.ON_STOP)

        assertEmptyList(emmittedItems)

        testScheduler.advanceTimeBy(5 , TimeUnit.SECONDS)

        assertListHasSize(emmittedItems, 1)

        //Assert
        assertFalse(disposable1.isDisposed)
        assertFalse(disposable2.isDisposed)
        assertFalse(disposeBag.isDisposed)

        lifecycleOwner.performStop()

        assertTrue(disposable1.isDisposed)
        assertTrue(disposable2.isDisposed)
        assertTrue(disposeBag.isDisposed)
    }

    @Test
    fun shouldDisposeWithLifeCycleWhenDefaultEndEventOccurs() {
        val emittedItems = mutableListOf<Long>()

        Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                    emittedItems.add(it)
                    System.out.println(it)
                }.disposedWith(lifecycleOwner)

        assertEmptyList(emittedItems)

        testScheduler.advanceTimeBy(20, TimeUnit.SECONDS)

        assertListHasSize(emittedItems, 4)

        lifecycleOwner.performDestroy()

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS)

        assertListHasSize(emittedItems, 4)
    }

    @Test
    fun shouldDisposeWithLifecycleWhenCustomEventOccurs() {
        testLifecycleWithCustomEvent(Lifecycle.State.CREATED, Lifecycle.Event.ON_DESTROY)
        testLifecycleWithCustomEvent(Lifecycle.State.RESUMED, Lifecycle.Event.ON_PAUSE)
        testLifecycleWithCustomEvent(Lifecycle.State.STARTED, Lifecycle.Event.ON_STOP)
    }

    private fun testLifecycleWithCustomEvent(beginState: Lifecycle.State, endEvent: Lifecycle.Event) {
        lifecycleOwner.lifecycleRegistry.markState(beginState)

        val disposable = Observable.fromIterable(1..100)
                .delay(10, TimeUnit.SECONDS)
                .subscribe {
                }.apply { disposedWith(lifecycleOwner, endEvent) }

        assertFalse(disposable.isDisposed)

        lifecycleOwner.performEvent(endEvent)

        assertTrue(disposable.isDisposed)
    }

    @Test
    fun overrideGlobalEventShouldDisposeOnGlobalEventEmission(){
        DisposeBagPlugins.defaultLifecycleDisposeEvent = Lifecycle.Event.ON_PAUSE

        lifecycleOwner.lifecycleRegistry.markState(Lifecycle.State.RESUMED)
        disposeBag = DisposeBag(lifecycleOwner)

        val disposable = Observable.interval(5, TimeUnit.SECONDS, testScheduler)
                .subscribe {
                }.apply { disposedBy(disposeBag) }

        assertFalse(disposable.isDisposed)
        assertFalse(disposeBag.isDisposed)

        lifecycleOwner.performPause()

        assertTrue(disposable.isDisposed)
        assertTrue(disposeBag.isDisposed)
    }

    private fun assertEmptyList(emittedItems: MutableList<Long>) {
        assertTrue(emittedItems.isEmpty())
    }

    private fun assertListHasSize(emittedItems: MutableList<Long>, size: Int) {
        assertTrue(emittedItems.size == size)
    }

}