@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.sqrt

sealed interface IntVector<V: IntVector<V>> {
    val size: Int
    fun setScalar(scalar: Int): V
    fun setComponent(index: Int, value: Int): V
    fun getComponent(index: Int): Int

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Int)
    operator fun get(key: String): Int
    operator fun set(key: Char, value: Int)
    operator fun get(key: Char): Int

    fun clone(): V
    fun copy(other: V): V

    fun add(v: V): V
    fun addScalar(s: Int): V
    fun addVectors(a: V, b: V): V
    fun addScaledVector(v: V, s: Int): V

    fun sub(v: V): V
    fun subScalar(s: Int): V
    fun subVectors(a: V, b: V): V

    fun multiply(v: V): V
    fun multiplyScalar(scalar: Int): V
    fun multiplyScalar(scalar: Float): V
    fun multiplyVectors(a: V, b: V): V

    fun divide(v: V): V
    fun divideScalar(scalar: Int): V

    fun min(v: V): V
    fun max(v: V): V
    fun clamp(min: V, max: V): V
    fun clampScalar(minVal: Int, maxVal: Int): V

    fun negate(): V

    infix fun dot(v: V): Int
    fun lengthSq(): Int
    fun manhattanLength(): Int
    fun distanceToSquared(v: V): Int
    fun manhattanDistanceTo(v: V): Int

    fun fromArray(array: IntArray, offset: Int = 0): V
    fun toArray(): IntArray
    fun toArray(array: IntArray, offset: Int = 0): IntArray
}

inline fun <V: IntVector<V>> IntVector<V>.set(scalar: Int) = setScalar(scalar)
inline operator fun <V: IntVector<V>> IntVector<V>.set(index: Int, value: Int) { setComponent(index, value) }
inline operator fun <V: IntVector<V>> IntVector<V>.get(index: Int) = getComponent(index)
inline fun <V: IntVector<V>> IntVector<V>.add(s: Int): V = addScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.plus(v: V): V = this.clone().add(v)
inline operator fun <V: IntVector<V>> IntVector<V>.plus(s: Int): V = this.clone().addScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.plusAssign(v: V) { this.add(v) }
inline operator fun <V: IntVector<V>> IntVector<V>.plusAssign(s: Int) { this.addScalar(s) }
inline fun <V: IntVector<V>> IntVector<V>.sub(s: Int): V = subScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.minus(v: V): V = this.clone().sub(v)
inline operator fun <V: IntVector<V>> IntVector<V>.minus(s: Int): V = this.clone().subScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.minusAssign(v: V) { this.sub(v) }
inline operator fun <V: IntVector<V>> IntVector<V>.minusAssign(s: Int) { this.subScalar(s) }
inline fun <V: IntVector<V>> IntVector<V>.multiply(scalar: Int): V = multiplyScalar(scalar)
inline fun <V: IntVector<V>> IntVector<V>.multiply(scalar: Float): V = multiplyScalar(scalar)
inline operator fun <V: IntVector<V>> IntVector<V>.times(v: V): V = this.clone().multiply(v)
inline operator fun <V: IntVector<V>> IntVector<V>.times(s: Int): V = this.clone().multiplyScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.times(s: Float): V = this.clone().multiplyScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.timesAssign(v: V) { this.multiply(v) }
inline operator fun <V: IntVector<V>> IntVector<V>.timesAssign(s: Int) { this.multiplyScalar(s) }
inline operator fun <V: IntVector<V>> IntVector<V>.timesAssign(s: Float) { this.multiplyScalar(s) }
inline fun <V: IntVector<V>> IntVector<V>.divide(scalar: Int): V = divideScalar(scalar)
inline operator fun <V: IntVector<V>> IntVector<V>.div(v: V): V = this.clone().divide(v)
inline operator fun <V: IntVector<V>> IntVector<V>.div(s: Int): V = this.clone().divideScalar(s)
inline operator fun <V: IntVector<V>> IntVector<V>.divAssign(v: V) { this.divide(v) }
inline operator fun <V: IntVector<V>> IntVector<V>.divAssign(s: Int) { this.divideScalar(s) }
inline fun <V: IntVector<V>> IntVector<V>.clamp(minVal: Int, maxVal: Int) = clampScalar(minVal, maxVal)
inline operator fun <V: IntVector<V>> IntVector<V>.unaryMinus(): V = this.clone().negate()
inline fun <V: IntVector<V>> IntVector<V>.length() = sqrt(this.lengthSq().toFloat())
inline fun <V: IntVector<V>> IntVector<V>.distanceTo(v: V) = sqrt(this.distanceToSquared(v).toFloat())
