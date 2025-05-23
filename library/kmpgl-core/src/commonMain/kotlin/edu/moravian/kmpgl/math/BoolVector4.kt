@file:Suppress("unused", "MemberVisibilityCanBePrivate", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class BoolVector4(
    @JvmField var x: Boolean = false,
    @JvmField var y: Boolean = false,
    @JvmField var z: Boolean = false,
    @JvmField var w: Boolean = false
): BoolVector<BoolVector4> {
    override val size: Int get() = 4

    inline fun set(x: Boolean = this.x,
                   y: Boolean = this.y,
                   z: Boolean = this.z,
                   w: Boolean = this.w): BoolVector4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override inline fun setScalar(scalar: Boolean): BoolVector4 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        this.w = scalar
        return this
    }

    inline fun setX(x: Boolean): BoolVector4 {
        this.x = x
        return this
    }
    inline fun setY(y: Boolean): BoolVector4 {
        this.y = y
        return this
    }
    inline fun setZ(z: Boolean): BoolVector4 {
        this.z = z
        return this
    }
    inline fun setW(w: Boolean): BoolVector4 {
        this.w = w
        return this
    }

    override inline fun setComponent(index: Int, value: Boolean): BoolVector4 {
        when (index) {
            0 -> this.x = value
            1 -> this.y = value
            2 -> this.z = value
            3 -> this.w = value
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }
        return this
    }

    override inline fun getComponent(index: Int) =
        when (index) {
            0 -> this.x
            1 -> this.y
            2 -> this.z
            3 -> this.w
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }

    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: Boolean) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            "w" -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Boolean =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            "w" -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Boolean) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            'w' -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Boolean =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            'w' -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = BoolVector4(this.x, this.y, this.z, this.w)
    override inline fun copy(other: BoolVector4) = set(other.x, other.y, other.z, other.w)

    override inline fun and(v: BoolVector4): BoolVector4 {
        this.x = this.x && v.x
        this.y = this.y && v.y
        this.z = this.z && v.z
        this.w = this.w && v.w
        return this
    }
    override inline fun or(v: BoolVector4): BoolVector4 {
        this.x = this.x || v.x
        this.y = this.y || v.y
        this.z = this.z || v.z
        this.w = this.w || v.w
        return this
    }
    override inline fun xor(v: BoolVector4): BoolVector4 {
        this.x = this.x != v.x
        this.y = this.y != v.y
        this.z = this.z != v.z
        this.w = this.w != v.w
        return this
    }
    override inline fun not(): BoolVector4 {
        this.x = !this.x
        this.y = !this.y
        this.z = !this.z
        this.w = !this.w
        return this
    }
    override  inline fun all() = this.x && this.y && this.z && this.w
    override inline fun any() = this.x || this.y || this.z || this.w
    override inline fun count() = (if (this.x) 1 else 0) + (if (this.y) 1 else 0) + (if (this.z) 1 else 0) + (if (this.w) 1 else 0)

    inline fun toVector() = Vector4(if (this.x) 1f else 0f, if (this.y) 1f else 0f, if (this.z) 1f else 0f, if (this.w) 1f else 0f)
    inline fun toIntVector() = IntVector4(if (this.x) 1 else 0, if (this.y) 1 else 0, if (this.z) 1 else 0, if (this.w) 1 else 0)
    inline fun toUIntVector() = UIntVector4(if (this.x) 1u else 0u, if (this.y) 1u else 0u, if (this.z) 1u else 0u, if (this.w) 1u else 0u)

    override fun fromArray(array: BooleanArray, offset: Int): BoolVector4 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        this.w = array[offset + 3]
        return this
    }
    override inline fun toArray(): BooleanArray = booleanArrayOf(this.x, this.y, this.z, this.w)
    override fun toArray(array: BooleanArray, offset: Int): BooleanArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        array[offset + 3] = this.w
        return array
    }
}
