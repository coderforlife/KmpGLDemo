@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.*

class UIntVector2(
    var x: UInt = 0u,
    var y: UInt = 0u
): UIntVector<UIntVector2> {
    override val size: Int get() = 2

    var width: UInt
        get() = this.x
        set(value) { this.x = value }
    var height: UInt
        get() = this.y
        set(value) { this.y = value }

    inline fun set(x: UInt = this.x,
                   y: UInt = this.y): UIntVector2 {
        this.x = x
        this.y = y
        return this
    }

    override inline fun setScalar(scalar: UInt): UIntVector2 {
        this.x = scalar
        this.y = scalar
        return this
    }

    inline fun setX(x: UInt): UIntVector2 {
        this.x = x
        return this
    }
    inline fun setY(y: UInt): UIntVector2 {
        this.y = y
        return this
    }

    override inline fun setComponent(index: Int, value: UInt): UIntVector2 {
        when (index) {
            0 -> this.x = value
            1 -> this.y = value
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }
        return this
    }
    // This conflicts with the set() method used for all of the other types...
    //inline operator fun set(index: Int, value: Int) { setComponent(index, value) }

    override inline fun getComponent(index: Int) =
        when (index) {
            0 -> this.x
            1 -> this.y
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }

    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: UInt) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): UInt =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: UInt) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): UInt =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = UIntVector2(this.x, this.y)
    override inline fun copy(other: UIntVector2) = this.set(other.x, other.y)

    override inline fun add(v: UIntVector2): UIntVector2 {
        this.x += v.x
        this.y += v.y
        return this
    }
    override inline fun addScalar(s: UInt): UIntVector2 {
        this.x += s
        this.y += s
        return this
    }
    override inline fun addVectors(a: UIntVector2, b: UIntVector2): UIntVector2 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        return this
    }
    override inline fun addScaledVector(v: UIntVector2, s: UInt): UIntVector2 {
        this.x += v.x * s
        this.y += v.y * s
        return this
    }

    override inline fun sub(v: UIntVector2): UIntVector2 {
        this.x -= v.x
        this.y -= v.y
        return this
    }
    override inline fun subScalar(s: UInt): UIntVector2 {
        this.x -= s
        this.y -= s
        return this
    }
    override inline fun subVectors(a: UIntVector2, b: UIntVector2): UIntVector2 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        return this
    }

    override inline fun multiply(v: UIntVector2): UIntVector2 {
        this.x *= v.x
        this.y *= v.y
        return this
    }
    override inline fun multiplyScalar(scalar: UInt): UIntVector2 {
        this.x *= scalar
        this.y *= scalar
        return this
    }
    override inline fun multiplyScalar(scalar: Float): UIntVector2 {
        this.x = (this.x.toDouble() * scalar).toUInt()
        this.y = (this.y.toDouble() * scalar).toUInt()
        return this
    }
    override inline fun multiplyVectors(a: UIntVector2, b: UIntVector2): UIntVector2 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        return this
    }

    override inline fun divide(v: UIntVector2): UIntVector2 {
        this.x /= v.x
        this.y /= v.y
        return this
    }
    override inline fun divideScalar(scalar: UInt): UIntVector2 {
        this.x /= scalar
        this.y /= scalar
        return this
    }

    override inline fun min(v: UIntVector2): UIntVector2 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        return this
    }

    override inline fun max(v: UIntVector2): UIntVector2 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        return this
    }

    override inline fun clamp(min: UIntVector2, max: UIntVector2): UIntVector2 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        return this
    }
    override inline fun clampScalar(minVal: UInt, maxVal: UInt): UIntVector2 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        return this
    }

    override inline infix fun dot(v: UIntVector2) = this.x * v.x + this.y * v.y
    inline infix fun cross(v: UIntVector2) = this.x * v.y - this.y * v.x
    inline operator fun rem(v: UIntVector2) = this.cross(v)
    override inline fun lengthSq() = this.x * this.x + this.y * this.y
    override inline fun manhattanLength() = this.x + this.y

    override inline fun distanceToSquared(v: UIntVector2): UInt {
        val dx = this.x - v.x; val dy = this.y - v.y
        return dx * dx + dy * dy
    }
    override inline fun manhattanDistanceTo(v: UIntVector2): UInt =
        absDiff(this.x, v.x) + absDiff(this.y, v.y)

    inline fun toVector() = Vector2(this.x.toFloat(), this.y.toFloat())
    inline fun toIntVector() = IntVector2(this.x.toInt(), this.y.toInt())
    inline fun toBoolVector() = BoolVector2(this.x != 0u, this.y != 0u)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun fromArray(array: UIntArray, offset: Int): UIntVector2 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        return this
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    override inline fun toArray() = uintArrayOf(this.x, this.y)
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toArray(array: UIntArray, offset: Int): UIntArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        return array
    }

    companion object { val pool = Pool { UIntVector2() } }
}