package com.kizitonwose.android.disposebag

import android.arch.lifecycle.Lifecycle
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
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

    @Test
    fun disposeByDisposeBag() {
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
    fun disposeByLifecycle() {
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
}