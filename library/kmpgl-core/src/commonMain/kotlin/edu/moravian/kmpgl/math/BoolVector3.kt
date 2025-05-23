@file:Suppress("unused", "MemberVisibilityCanBePrivate", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class BoolVector3(
    @JvmField var x: Boolean = false,
    @JvmField var y: Boolean = false,
    @JvmField var z: Boolean = false
): BoolVector<BoolVector3> {
    override val size: Int get() = 3

    inline fun set(x: Boolean = this.x,
                   y: Boolean = this.y,
                   z: Boolean = this.z): BoolVector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override inline fun setScalar(scalar: Boolean): BoolVector3 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        return this
    }

    inline fun setX(x: Boolean): BoolVector3 {
        this.x = x
        return this
    }
    inline fun setY(y: Boolean): BoolVector3 {
        this.y = y
        return this
    }
    inline fun setZ(z: Boolean): BoolVector3 {
        this.z = z
        return this
    }

    override inline fun setComponent(index: Int, value: Boolean): BoolVector3 {
        when (index) {
            0 -> this.x = value
            1 -> this.y = value
            2 -> this.z = value
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }
        return this
    }

    override inline fun getComponent(index: Int) =
        when (index) {
            0 -> this.x
            1 -> this.y
            2 -> this.z
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }

    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: Boolean) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Boolean =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Boolean) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Boolean =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = BoolVector3(this.x, this.y, this.z)
    override inline fun copy(other: BoolVector3) = set(other.x, other.y, other.z)

    override inline fun and(v: BoolVector3): BoolVector3 {
        this.x = this.x && v.x
        this.y = this.y && v.y
        this.z = this.z && v.z
        return this
    }
    override inline fun or(v: BoolVector3): BoolVector3 {
        this.x = this.x || v.x
        this.y = this.y || v.y
        this.z = this.z || v.z
        return this
    }
    override inline fun xor(v: BoolVector3): BoolVector3 {
        this.x = this.x != v.x
        this.y = this.y != v.y
        this.z = this.z != v.z
        return this
    }
    override inline fun not(): BoolVector3 {
        this.x = !this.x
        this.y = !this.y
        this.z = !this.z
        return this
    }
    override inline fun all() = this.x && this.y && this.z
    override inline fun any() = this.x || this.y || this.z
    override inline fun count() = (if (this.x) 1 else 0) + (if (this.y) 1 else 0) + (if (this.z) 1 else 0)

    inline fun toVector() = Vector3(if (this.x) 1f else 0f, if (this.y) 1f else 0f, if (this.z) 1f else 0f)
    inline fun toIntVector() = IntVector3(if (this.x) 1 else 0, if (this.y) 1 else 0, if (this.z) 1 else 0)
    inline fun toUIntVector() = UIntVector3(if (this.x) 1u else 0u, if (this.y) 1u else 0u, if (this.z) 1u else 0u)

    override fun fromArray(array: BooleanArray, offset: Int): BoolVector3 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        return this
    }
    override inline fun toArray(): BooleanArray = booleanArrayOf(this.x, this.y, this.z)
    override fun toArray(array: BooleanArray, offset: Int): BooleanArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        return array
    }
}
