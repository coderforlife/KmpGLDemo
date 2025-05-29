package edu.moravian.kmpgl.core

import platform.Foundation.NSCondition
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.performBlock

internal fun <T> NSRunLoop.performSync(action: () -> T): T {
    var retval: T? = null
    var exception: Throwable? = null
    val condition = NSCondition()
    condition.lockAndWait {
        this.performBlock {
            try {
                retval = condition.lockAndSignal(action)
            } catch (e: Throwable) {
                exception = e
                e.printStackTrace()
            }
        }
    }
    exception?.let { throw it }
    return retval!!
}

// This function locks the NSCondition, executes the block, waits for a signal
// The block that is executed should asynchronously utilize lockAndSignal()
internal inline fun <T> NSCondition.lockAndWait(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        wait()
        unlock()
    }
}

// This function locks the NSCondition, executes the block, signals the condition
// This should be used in conjunction with lockAndWait() to ensure that the condition is signaled after the block is executed
internal inline fun <T> NSCondition.lockAndSignal(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        signal()
        unlock()
    }
}

internal inline fun <T> NSLock.withLock(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}
