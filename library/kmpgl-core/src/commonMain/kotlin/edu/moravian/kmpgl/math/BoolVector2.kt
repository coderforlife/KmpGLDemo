@file:Suppress("unused", "MemberVisibilityCanBePrivate", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class BoolVector2(
    @JvmField var x: Boolean = false,
    @JvmField var y: Boolean = false
): BoolVector<BoolVector2> {
    override val size: Int get() = 2

    inline fun set(x: Boolean = this.x,
                   y: Boolean = this.y): BoolVector2 {
        this.x = x
        this.y = y
        return this
    }

    override inline fun setScalar(scalar: Boolean): BoolVector2 {
        this.x = scalar
        this.y = scalar
        return this
    }

    inline fun setX(x: Boolean): BoolVector2 {
        this.x = x
        return this
    }
    inline fun setY(y: Boolean): BoolVector2 {
        this.y = y
        return this
    }

    override inline fun setComponent(index: Int, value: Boolean): BoolVector2 {
        when (index) {
            0 -> this.x = value
            1 -> this.y = value
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }
        return this
    }

    override inline fun getComponent(index: Int) =
        when (index) {
            0 -> this.x
            1 -> this.y
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }

    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: Boolean) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Boolean =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Boolean) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Boolean =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() =
        BoolVector2(this.x, this.y)
    override inline fun copy(other: BoolVector2) = set(other.x, other.y)

    override inline fun and(v: BoolVector2): BoolVector2 {
        this.x = this.x && v.x
        this.y = this.y && v.y
        return this
    }
    override inline fun or(v: BoolVector2): BoolVector2 {
        this.x = this.x || v.x
        this.y = this.y || v.y
        return this
    }
    override inline fun xor(v: BoolVector2): BoolVector2 {
        this.x = this.x != v.x
        this.y = this.y != v.y
        return this
    }
    override inline fun not(): BoolVector2 {
        this.x = !this.x
        this.y = !this.y
        return this
    }
    override inline fun all() = this.x && this.y
    override inline fun any() = this.x || this.y
    override inline fun count() = (if (this.x) 1 else 0) + (if (this.y) 1 else 0)

    inline fun toVector() = Vector2(if (this.x) 1f else 0f, if (this.y) 1f else 0f)
    inline fun toIntVector() = IntVector2(if (this.x) 1 else 0, if (this.y) 1 else 0)
    inline fun toUIntVector() = UIntVector2(if (this.x) 1u else 0u, if (this.y) 1u else 0u)

    override fun fromArray(array: BooleanArray, offset: Int): BoolVector2 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        return this
    }
    override inline fun toArray(): BooleanArray = booleanArrayOf(this.x, this.y)
    override fun toArray(array: BooleanArray, offset: Int): BooleanArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        return array
    }
}
