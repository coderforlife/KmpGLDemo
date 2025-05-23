@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

class IntVector2(
    @JvmField var x: Int = 0,
    @JvmField var y: Int = 0
): IntVector<IntVector2> {
    override val size: Int get() = 2

    var width: Int
        get() = this.x
        set(value) { this.x = value }
    var height: Int
        get() = this.y
        set(value) { this.y = value }

    inline fun set(x: Int = this.x,
                   y: Int = this.y): IntVector2 {
        this.x = x
        this.y = y
        return this
    }

    override inline fun setScalar(scalar: Int): IntVector2 {
        this.x = scalar
        this.y = scalar
        return this
    }

    inline fun setX(x: Int): IntVector2 {
        this.x = x
        return this
    }
    inline fun setY(y: Int): IntVector2 {
        this.y = y
        return this
    }

    override inline fun setComponent(index: Int, value: Int): IntVector2 {
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
    override operator fun set(key: String, value: Int) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Int =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Int) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Int =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = IntVector2(this.x, this.y)
    override inline fun copy(other: IntVector2) = this.set(other.x, other.y)

    override inline fun add(v: IntVector2): IntVector2 {
        this.x += v.x
        this.y += v.y
        return this
    }
    override inline fun addScalar(s: Int): IntVector2 {
        this.x += s
        this.y += s
        return this
    }
    override inline fun addVectors(a: IntVector2, b: IntVector2): IntVector2 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        return this
    }
    override inline fun addScaledVector(v: IntVector2, s: Int): IntVector2 {
        this.x += v.x * s
        this.y += v.y * s
        return this
    }

    override inline fun sub(v: IntVector2): IntVector2 {
        this.x -= v.x
        this.y -= v.y
        return this
    }
    override inline fun subScalar(s: Int): IntVector2 {
        this.x -= s
        this.y -= s
        return this
    }
    override inline fun subVectors(a: IntVector2, b: IntVector2): IntVector2 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        return this
    }

    override inline fun multiply(v: IntVector2): IntVector2 {
        this.x *= v.x
        this.y *= v.y
        return this
    }
    override inline fun multiplyScalar(scalar: Int): IntVector2 {
        this.x *= scalar
        this.y *= scalar
        return this
    }
    override inline fun multiplyScalar(scalar: Float): IntVector2 {
        this.x = (this.x * scalar).toInt()
        this.y = (this.y * scalar).toInt()
        return this
    }
    override inline fun multiplyVectors(a: IntVector2, b: IntVector2): IntVector2 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        return this
    }

    override inline fun divide(v: IntVector2): IntVector2 {
        this.x /= v.x
        this.y /= v.y
        return this
    }
    override inline fun divideScalar(scalar: Int): IntVector2 {
        this.x /= scalar
        this.y /= scalar
        return this
    }

    override inline fun min(v: IntVector2): IntVector2 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        return this
    }

    override inline fun max(v: IntVector2): IntVector2 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        return this
    }

    override inline fun clamp(min: IntVector2, max: IntVector2): IntVector2 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        return this
    }
    override inline fun clampScalar(minVal: Int, maxVal: Int): IntVector2 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        return this
    }

    override inline fun negate(): IntVector2 {
        this.x = -this.x
        this.y = -this.y
        return this
    }

    override inline infix fun dot(v: IntVector2) = this.x * v.x + this.y * v.y
    inline infix fun cross(v: IntVector2) = this.x * v.y - this.y * v.x
    inline operator fun rem(v: IntVector2) = this.cross(v)
    override inline fun lengthSq() = this.x * this.x + this.y * this.y
    override inline fun manhattanLength() = abs(this.x) + abs(this.y)

    override inline fun distanceToSquared(v: IntVector2): Int {
        val dx = this.x - v.x; val dy = this.y - v.y
        return dx * dx + dy * dy
    }
    override inline fun manhattanDistanceTo(v: IntVector2): Int =
        abs(this.x - v.x) + abs(this.y - v.y)

    inline fun toVector() = Vector2(this.x.toFloat(), this.y.toFloat())
    inline fun toUIntVector() = UIntVector2(this.x.toUInt(), this.y.toUInt())
    inline fun toBoolVector() = BoolVector2(this.x != 0, this.y != 0)

    override fun fromArray(array: IntArray, offset: Int): IntVector2 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        return this
    }
    override inline fun toArray(): IntArray = intArrayOf(this.x, this.y)
    override fun toArray(array: IntArray, offset: Int): IntArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        return array
    }

    companion object { val pool = Pool { IntVector2() } }
}