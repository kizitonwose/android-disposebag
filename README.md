# Android-DisposeBag

[![Build Status](https://travis-ci.org/kizitonwose/android-disposebag.svg?branch=master)](https://travis-ci.org/kizitonwose/android-disposebag) 
[![Coverage](https://img.shields.io/codecov/c/github/kizitonwose/android-disposebag/master.svg)](https://codecov.io/gh/kizitonwose/android-disposebag) 
[![JitPack](https://jitpack.io/v/kizitonwose/android-disposebag.svg)](https://jitpack.io/#kizitonwose/android-disposebag) 
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/kizitonwose/android-disposebag/blob/master/LICENSE.md) 

[RxSwift][rxswift-url] has an inbuilt [DisposeBag][disposebag-swift-url] container which disposes all disposables when it is deinitialized. Unfortunately, there is no reliable way to achieve the same result in Java/Kotlin. Even if we could achieve this, there's still a problem with the Android platform, Activities are created and managed by the system, using it after either `onDestroy` or `onStop` method is called will result to a crash. 

This library uses the new LifecycleObserver introduced in Android Architecture Components to automatically dispose [RxJava][rxjava-url]/[RxKotlin][rxkotlin-url] streams at the right time.

## Usage

### Using a DisposeBag

Create a DisposeBag, supply your LifecycleOwner, then add all your disposbles.

The example below uses an Activity but this also works with Fragments or any other class that impements the LifecycleOwner interface.

##### Kotlin:

```kotlin
class MainActivity : AppCompatActivity() {
    
    val bag = DisposeBag(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val button = findViewById<Button>(R.id.button)
        
        button.clicks()
                .subscribe {
                    // Handle button clicks
                }.disposedBy(bag)
    }
}
```

##### Java:

```java
public final class MainActivity extends AppCompatActivity {

    final DisposeBag bag = DisposeBag(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Button button = findViewById(R.id.button);

        bag.add(RxView.clicks(button)
                .subscribe(o -> {
                    // Handle button clicks 
                }));
    }
}
```

In the examples above, the DisposeBag automatically disposes all disposables when the activity is destroyed. The `clicks()` extension function and `RxView.clicks()` static method are both from [RxBinding][rxbinding-url]. Internally, the DisposeBag uses a [CompositeDisposable][compositedisposable-java-url].

You can change the dipose event by specifying it when creating the DisposeBag:

##### Kotlin:

```kotlin
// This is disposed at the "on stop" event
val bag = DisposeBag(this, Lifecycle.Event.ON_STOP)
```

##### Java:

```java
// This is disposed at the "on stop" event
DisposeBag bag = new DisposeBag(this, Lifecycle.Event.ON_STOP);
```

### Using a LifecycleOwner

Since the DisposeBag basically just acts on lifecycle events, you can directly use the LifecycleOwner to dispose your disposables without having to first create the DisposeBag.

The example below uses a Fragment but of course you can use an Activity or any other LifeCycleOwner.

```kotlin
class MainFragment : Fragment() {
        
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val button = view.findViewById<Button>(R.id.button)
        
        button.clicks()
                .subscribe {
                    // Handle button clicks
                }.disposedWith(this)
    }

}
```

You can also change the event which triggers the disposal, default is `Lifecycle.Event.ON_DESTROY`

```kotlin
button.clicks()
        .subscribe {
            // Handle button clicks
        }.disposedWith(this, Lifecycle.Event.ON_STOP) // Change the dispose event
```

> Note the difference between the two: `disposedBy()` and `disposedWith()`

## Changing the default dispose event globally

If you would like to change the default dispose event, you can do this via the `DisposeBagPlugins`

In your app's Application class:

##### Kotlin:

```kotlin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DisposeBagPlugins.defaultLifecycleDisposeEvent = Lifecycle.Event.ON_STOP
    }

}
```

##### Java:

```java
public final class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DisposeBagPlugins.setDefaultLifecycleDisposeEvent(Lifecycle.Event.ON_STOP);
    }
}
```

And from then, your app's default dispose event will be `Lifecycle.Event.ON_STOP` instead of `Lifecycle.Event.ON_DESTROY`

## Installation

Add the JitPack repository to your `build.gradle`:

```groovy
allprojects {
 repositories {
    maven { url "https://jitpack.io" }
    }
}
```

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.kizitonwose:android-disposebag:0.1.0'
}
```

## Contributing

This library is a combination of some extension functions and classes I wrote in a project of mine, improvements are welcome.

## License

Distributed under the MIT license. [See LICENSE](https://github.com/kizitonwose/android-disposebag/blob/master/LICENSE.md) for details.

[rxswift-url]: https://github.com/ReactiveX/RxSwift 
[disposebag-swift-url]: https://github.com/ReactiveX/RxSwift/blob/master/RxSwift/Disposables/DisposeBag.swift 
[rxjava-url]: https://github.com/ReactiveX/RxJava 
[rxkotlin-url]: https://github.com/ReactiveX/RxKotlin
[rxbinding-url]: https://github.com/JakeWharton/RxBinding
[compositedisposable-java-url]: https://github.com/ReactiveX/RxJava/blob/2.x/src/main/java/io/reactivex/disposables/CompositeDisposable.java

