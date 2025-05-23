KMM-GL
======

Module that provides multiplatform access to OpenGL ES. Supports Android and iOS but could support additional platforms as well.

It aims to mimic a WebGLRenderingContext interface, although there are significant differences. See the `GLContext` class documentation for more information.

On Android this wraps `GLSurfaceView`. On iOS this wraps `GLKView`.

The main class is `GLContext` for OpenGL ES 2.0 (i.e. WebGL) support or `GLContext30` for OpenGL ES 3.0 (i.e. WebGL2) support. Within those classes, all OpenGL functions are available but drop the `gl` prefix. The context object must be initialized on a specific platform. The initialization accepts many attributes during creation (e.g. `alpha` or `antialias` options).

The context object creates and manages the view (i.e. canvas) instead of it being created outside of the class (use the `init()` or `initIfNeeded()` methods with a `GLPlatformContext` object that contains platform-specific data).

The context object manages the rendering loop directly (although it can also be managed externally) and listeners are registered to be able to create resources at the right time, render frames, and cleanup.

You must run things that interact with GL on the rendering thread. You can use `isOnRenderThread()`, `runAsync()`, and `runSync()` to help with this. All of the functions called by the listeners are automatically run on the rendering thread.

No GL constants are defined within the context, instead they are available in the singleton object `GL`.

There is no `getParameter()` but instead type-specific  functions. Parameters like `border` that must always be a specific value have been removed or defaulted; postfix argument types like "i" or "f" sometimes dropped from the names.

On any system, the biggest task it to implement a `GLListener` instance. This is where all the logic for the 3D program will be implemented (primarily in `create()`/`recreate()` for loading resources and `render()` to draw a frame).

A `GLContext` object can only be initialized once, but can be reused (the view can be moved to another layout for example). The context and view can only be used once at a time in a particular layout though, so if additional views are needed in the same layout, additional contexts will be required.

Android
-------

In your `Activity` class, make a new variable for your GL context:

```kotlin
private val glContext = GLContext()
```

In `onCreate()` or elsewhere:

```kotlin
glContext.addListener(listener) // add first to make sure we get the initial create() event
findViewById(R.id.rootView).addView(glContext.initIfNeeded(context = GLPlatformContext(context))) // can also take GLContextAttributes to adjust the surface created
```

iOS - SwiftUI
-------------

Add the following wrapper class:

```swift
import SwiftUI
import UIKit
import kmm_gl
struct GLView: UIViewControllerRepresentable {
    let glContext = GLContext()
    func makeUIViewController(context: Context) -> UIViewController {
        glContext.addListener(listener: listener)
        return glContext.doInitIfNeeded()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        glContext.viewController.viewDidLoad()
    }
}
```

Then you can use it in your other views, for example:

```swift
GLView().frame(width: 375, height: 375)
```

Compose
-------

A multiplatform compose wrapper is provided in the `kmpgl-compose` module supporting Android and iOS.


TODO
----

- Test more of the API
- Support additional extensions
- Add coroutine support

TODO in the way future
----------------------

- Support JS
- Support JVM
- Support Desktop
