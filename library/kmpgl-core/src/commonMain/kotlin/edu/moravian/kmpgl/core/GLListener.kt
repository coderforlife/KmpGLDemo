package edu.moravian.kmpgl.core

interface GLListener {
    /** Called once when the application is created */
    fun create(gl: GLContext) { }

    /** Called whenever the application is re-created after being lost */
    fun recreate(gl: GLContext) { }

    /**
     * This method is called every time the view is re-sized and not in the
     * paused state. It is also called once just after the create() or recreate()
     * methods. The parameters are the new width and height the view has been
     * resized to in pixels.
     */
    fun resize(gl: GLContext, width: Int, height: Int) { }

    /**
     * Method called every time rendering should be performed. This method
     * should be very efficient and avoid things like memory allocations. The
     * passed time is in nanoseconds (although the actual precision could be
     * much less), is monotonically increasing with an arbitrary origin, and
     * does not necessarily account for device sleep.
     *
     * Important: context.addListener() or context.removeListener() must not be
     * called in this method.
     */
    fun render(gl: GLContext, time: Long) { }

    /**
     * On Android this method is called when the Home button is pressed or an
     * incoming call is received. A good place to save state.
     */
    fun pause(gl: GLContext) { }

    /**
     * This method is called when the application resumes from a paused state.
     * It also is called once after create()/recreate() before the first
     * render() call.
     */
    fun resume(gl: GLContext) { }

    /**
     * Called when the application is destroyed. It is preceded by a call to
     * pause().
     */
    fun dispose(gl: GLContext) { }
}
