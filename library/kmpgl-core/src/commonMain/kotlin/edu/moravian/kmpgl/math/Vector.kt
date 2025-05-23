@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.sqrt

sealed interface Vector<V: Vector<V>> {
    val size: Int

    fun setScalar(scalar: Float): V
    fun setComponent(index: Int, value: Float): V
    fun getComponent(index: Int): Float

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Float)
    operator fun get(key: String): Float
    operator fun set(key: Char, value: Float)
    operator fun get(key: Char): Float

    fun clone(): V
    fun copy(other: V): V

    fun add(v: V): V
    fun addScalar(s: Float): V
    fun addVectors(a: V, b: V): V
    fun addScaledVector(v: V, s: Float): V

    fun sub(v: V): V
    fun subScalar(s: Float): V
    fun subVectors(a: V, b: V): V

    fun multiply(v: V): V
    fun multiplyScalar(scalar: Float): V
    fun multiplyVectors(a: V, b: V): V

    fun divide(v: V): V

    fun min(v: V): V
    fun max(v: V): V
    fun clamp(min: V, max: V): V
    fun clampScalar(minVal: Float, maxVal: Float): V
    fun floor(): V
    fun ceil(): V
    fun round(): V
    fun roundToZero(): V

    fun negate(): V

    infix fun dot(v: V): Float
    fun lengthSq(): Float
    fun manhattanLength(): Float

    fun lerp(v: V, alpha: Float): V
    fun lerpVectors(v1: V, v2: V, alpha: Float): V

    fun distanceToSquared(v: V): Float
    fun manhattanDistanceTo(v: V): Float

    fun fromArray(array: FloatArray, offset: Int = 0): V
    fun toArray(): FloatArray
    fun toArray(array: FloatArray, offset: Int = 0): FloatArray

    fun random(): V
}

inline fun <V: Vector<V>> Vector<V>.set(scalar: Float) = setScalar(scalar)
inline operator fun <V: Vector<V>> Vector<V>.set(index: Int, value: Float) { setComponent(index, value) }
inline operator fun <V: Vector<V>> Vector<V>.get(index: Int) = getComponent(index)
inline fun <V: Vector<V>> Vector<V>.add(s: Float): V = addScalar(s)
inline operator fun <V: Vector<V>> Vector<V>.plus(v: V): V = this.clone().add(v)
inline operator fun <V: Vector<V>> Vector<V>.plus(s: Float): V = this.clone().addScalar(s)
inline operator fun <V: Vector<V>> Vector<V>.plusAssign(v: V) { this.add(v) }
inline operator fun <V: Vector<V>> Vector<V>.plusAssign(s: Float) { this.addScalar(s) }
inline fun <V: Vector<V>> Vector<V>.sub(s: Float): V = subScalar(s)
inline operator fun <V: Vector<V>> Vector<V>.minus(v: V): V = this.clone().sub(v)
inline operator fun <V: Vector<V>> Vector<V>.minus(s: Float): V = this.clone().subScalar(s)
inline operator fun <V: Vector<V>> Vector<V>.minusAssign(v: V) { this.sub(v) }
inline operator fun <V: Vector<V>> Vector<V>.minusAssign(s: Float) { this.subScalar(s) }
inline fun <V: Vector<V>> Vector<V>.multiply(scalar: Float): V = multiplyScalar(scalar)
inline operator fun <V: Vector<V>> Vector<V>.times(v: V): V = this.clone().multiply(v)
inline operator fun <V: Vector<V>> Vector<V>.times(s: Float): V = this.clone().multiplyScalar(s)
inline operator fun <V: Vector<V>> Vector<V>.timesAssign(v: V) { this.multiply(v) }
inline operator fun <V: Vector<V>> Vector<V>.timesAssign(s: Float) { this.multiplyScalar(s) }
inline fun <V: Vector<V>> Vector<V>.divideScalar(scalar: Float) = this.multiplyScalar(1f / scalar)
inline fun <V: Vector<V>> Vector<V>.divide(scalar: Float): V = divideScalar(scalar)
inline operator fun <V: Vector<V>> Vector<V>.div(v: V): V = this.clone().divide(v)
inline operator fun <V: Vector<V>> Vector<V>.div(s: Float): V = this.clone().divideScalar(s)
inline operator fun <V: Vector<V>> Vector<V>.divAssign(v: V) { this.divide(v) }
inline operator fun <V: Vector<V>> Vector<V>.divAssign(s: Float) { this.divideScalar(s) }
inline fun <V: Vector<V>> Vector<V>.clamp(minVal: Float, maxVal: Float) = clampScalar(minVal, maxVal)
inline fun <V: Vector<V>> Vector<V>.clampLength(min: Float, max: Float): V {
    val length = this.length()
    return if (length == 0f) { this } else { this.divideScalar(length) }.multiplyScalar(clamp(length, min, max))
}
inline operator fun <V: Vector<V>> Vector<V>.unaryMinus(): V = this.clone().negate()
inline fun <V: Vector<V>> Vector<V>.length() = sqrt(this.lengthSq())
@Suppress("UNCHECKED_CAST")
inline fun <V: Vector<V>> Vector<V>.normalize(): V = length().let { if (it == 0f) this else this.divideScalar(it) } as V
inline fun <V: Vector<V>> Vector<V>.setLength(length: Float) = this.normalize().multiplyScalar(length)
inline fun <V: Vector<V>> Vector<V>.distanceTo(v: V) = sqrt(this.distanceToSquared(v))
