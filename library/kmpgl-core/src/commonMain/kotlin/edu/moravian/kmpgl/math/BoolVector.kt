@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

sealed interface BoolVector<V: BoolVector<V>> {
    val size: Int

    fun setScalar(scalar: Boolean): V
    fun setComponent(index: Int, value: Boolean): V
    fun getComponent(index: Int): Boolean

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Boolean)
    operator fun get(key: String): Boolean
    operator fun set(key: Char, value: Boolean)
    operator fun get(key: Char): Boolean

    fun clone(): V
    fun copy(other: V): V

    fun and(v: V): V
    fun or(v: V): V
    fun xor(v: V): V
    fun not(): V
    fun all(): Boolean
    fun any(): Boolean
    fun count(): Int

    fun fromArray(array: BooleanArray, offset: Int = 0): V
    fun toArray(): BooleanArray
    fun toArray(array: BooleanArray, offset: Int = 0): BooleanArray
}

inline fun <V: BoolVector<V>> BoolVector<V>.set(scalar: Boolean) = setScalar(scalar)
inline operator fun <V: BoolVector<V>> BoolVector<V>.set(index: Int, value: Boolean) { setComponent(index, value) }
inline operator fun <V: BoolVector<V>> BoolVector<V>.get(index: Int) = getComponent(index)
