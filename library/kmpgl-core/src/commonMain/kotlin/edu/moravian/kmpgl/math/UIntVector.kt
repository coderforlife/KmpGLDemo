@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.sqrt

sealed interface UIntVector<V: UIntVector<V>> {
    val size: Int
    fun setScalar(scalar: UInt): V
    fun setComponent(index: Int, value: UInt): V
    fun getComponent(index: Int): UInt

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: UInt)
    operator fun get(key: String): UInt
    operator fun set(key: Char, value: UInt)
    operator fun get(key: Char): UInt

    fun clone(): V
    fun copy(other: V): V

    fun add(v: V): V
    fun addScalar(s: UInt): V
    fun addVectors(a: V, b: V): V
    fun addScaledVector(v: V, s: UInt): V

    fun sub(v: V): V
    fun subScalar(s: UInt): V
    fun subVectors(a: V, b: V): V

    fun multiply(v: V): V
    fun multiplyScalar(scalar: UInt): V
    fun multiplyScalar(scalar: Float): V
    fun multiplyVectors(a: V, b: V): V

    fun divide(v: V): V
    fun divideScalar(scalar: UInt): V

    fun min(v: V): V
    fun max(v: V): V
    fun clamp(min: V, max: V): V
    fun clampScalar(minVal: UInt, maxVal: UInt): V

    infix fun dot(v: V): UInt
    fun lengthSq(): UInt
    fun manhattanLength(): UInt
    fun distanceToSquared(v: V): UInt
    fun manhattanDistanceTo(v: V): UInt

    @OptIn(ExperimentalUnsignedTypes::class)
    fun fromArray(array: UIntArray, offset: Int = 0): V
    @OptIn(ExperimentalUnsignedTypes::class)
    fun toArray(): UIntArray
    @OptIn(ExperimentalUnsignedTypes::class)
    fun toArray(array: UIntArray, offset: Int = 0): UIntArray
}

inline fun <V: UIntVector<V>> UIntVector<V>.set(scalar: UInt) = setScalar(scalar)
inline operator fun <V: UIntVector<V>> UIntVector<V>.set(index: Int, value: UInt) { setComponent(index, value) }
inline operator fun <V: UIntVector<V>> UIntVector<V>.get(index: Int) = getComponent(index)
inline fun <V: UIntVector<V>> UIntVector<V>.add(s: UInt): V = addScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.plus(v: V): V = this.clone().add(v)
inline operator fun <V: UIntVector<V>> UIntVector<V>.plus(s: UInt): V = this.clone().addScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.plusAssign(v: V) { this.add(v) }
inline operator fun <V: UIntVector<V>> UIntVector<V>.plusAssign(s: UInt) { this.addScalar(s) }
inline fun <V: UIntVector<V>> UIntVector<V>.sub(s: UInt): V = subScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.minus(v: V): V = this.clone().sub(v)
inline operator fun <V: UIntVector<V>> UIntVector<V>.minus(s: UInt): V = this.clone().subScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.minusAssign(v: V) { this.sub(v) }
inline operator fun <V: UIntVector<V>> UIntVector<V>.minusAssign(s: UInt) { this.subScalar(s) }
inline fun <V: UIntVector<V>> UIntVector<V>.multiply(scalar: UInt): V = multiplyScalar(scalar)
inline fun <V: UIntVector<V>> UIntVector<V>.multiply(scalar: Float): V = multiplyScalar(scalar)
inline operator fun <V: UIntVector<V>> UIntVector<V>.times(v: V): V = this.clone().multiply(v)
inline operator fun <V: UIntVector<V>> UIntVector<V>.times(s: UInt): V = this.clone().multiplyScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.times(s: Float): V = this.clone().multiplyScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.timesAssign(v: V) { this.multiply(v) }
inline operator fun <V: UIntVector<V>> UIntVector<V>.timesAssign(s: UInt) { this.multiplyScalar(s) }
inline operator fun <V: UIntVector<V>> UIntVector<V>.timesAssign(s: Float) { this.multiplyScalar(s) }
inline fun <V: UIntVector<V>> UIntVector<V>.divide(scalar: UInt): V = divideScalar(scalar)
inline operator fun <V: UIntVector<V>> UIntVector<V>.div(v: V): V = this.clone().divide(v)
inline operator fun <V: UIntVector<V>> UIntVector<V>.div(s: UInt): V = this.clone().divideScalar(s)
inline operator fun <V: UIntVector<V>> UIntVector<V>.divAssign(v: V) { this.divide(v) }
inline operator fun <V: UIntVector<V>> UIntVector<V>.divAssign(s: UInt) { this.divideScalar(s) }
inline fun <V: UIntVector<V>> UIntVector<V>.clamp(minVal: UInt, maxVal: UInt) = clampScalar(minVal, maxVal)
inline fun <V: UIntVector<V>> UIntVector<V>.length() = sqrt(this.lengthSq().toFloat())
inline fun <V: UIntVector<V>> UIntVector<V>.distanceTo(v: V) = sqrt(this.distanceToSquared(v).toFloat())
