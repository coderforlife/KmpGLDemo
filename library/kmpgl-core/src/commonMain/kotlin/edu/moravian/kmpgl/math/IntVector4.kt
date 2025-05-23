@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class IntVector4(
    @JvmField var x: Int = 0,
    @JvmField var y: Int = 0,
    @JvmField var z: Int = 0,
    @JvmField var w: Int = 1
): IntVector<IntVector4> {
    override val size: Int get() = 4

    var width: Int
        get() = this.z
        set(value) { this.z = value }

    var height: Int
        get() = this.w
        set(value) { this.w = value }

    inline fun set(x: Int = this.x,
                   y: Int = this.y,
                   z: Int = this.z,
                   w: Int = this.w): IntVector4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override inline fun setScalar(scalar: Int): IntVector4 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        this.w = scalar
        return this
    }

    inline fun setX(x: Int): IntVector4 {
        this.x = x
        return this
    }
    inline fun setY(y: Int): IntVector4 {
        this.y = y
        return this
    }
    inline fun setZ(z: Int): IntVector4 {
        this.z = z
        return this
    }
    inline fun setW(w: Int): IntVector4 {
        this.w = w
        return this
    }

    override inline fun setComponent(index: Int, value: Int): IntVector4 {
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
    override operator fun set(key: String, value: Int) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            "w" -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Int =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            "w" -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Int) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            'w' -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Int =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            'w' -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = IntVector4(this.x, this.y, this.z, this.w)
    override inline fun copy(other: IntVector4) = set(other.x, other.y, other.z, other.w)

    override inline fun add(v: IntVector4): IntVector4 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        this.w += v.w
        return this
    }
    override inline fun addScalar(s: Int): IntVector4 {
        this.x += s
        this.y += s
        this.z += s
        this.w += s
        return this
    }
    override inline fun addVectors(a: IntVector4, b: IntVector4): IntVector4 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        this.w = a.w + b.w
        return this
    }
    override inline fun addScaledVector(v: IntVector4, s: Int): IntVector4 {
        this.x += v.x * s
        this.y += v.y * s
        this.z += v.z * s
        this.w += v.w * s
        return this
    }

    override inline fun sub(v: IntVector4): IntVector4 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        this.w -= v.w
        return this
    }
    override inline fun subScalar(s: Int): IntVector4 {
        this.x -= s
        this.y -= s
        this.z -= s
        this.w -= s
        return this
    }
    override inline fun subVectors(a: IntVector4, b: IntVector4): IntVector4 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        this.w = a.w - b.w
        return this
    }

    override inline fun multiply(v: IntVector4): IntVector4 {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        this.w *= v.w
        return this
    }
    override inline fun multiplyScalar(scalar: Int): IntVector4 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        this.w *= scalar
        return this
    }
    override inline fun multiplyScalar(scalar: Float): IntVector4 {
        this.x = (this.x * scalar).toInt()
        this.y = (this.y * scalar).toInt()
        this.z = (this.z * scalar).toInt()
        this.w = (this.w * scalar).toInt()
        return this
    }
    override inline fun multiplyVectors(a: IntVector4, b: IntVector4): IntVector4 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        this.w = a.w * b.w
        return this
    }

    override inline fun divide(v: IntVector4): IntVector4 {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        this.w /= v.w
        return this
    }
    override inline fun divideScalar(scalar: Int): IntVector4 {
        this.x /= scalar
        this.y /= scalar
        this.z /= scalar
        this.w /= scalar
        return this
    }

    override inline fun min(v: IntVector4): IntVector4 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        this.z = min(this.z, v.z)
        this.w = min(this.w, v.w)
        return this
    }

    override inline fun max(v: IntVector4): IntVector4 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        this.z = max(this.z, v.z)
        this.w = max(this.w, v.w)
        return this
    }

    override inline fun clamp(min: IntVector4, max: IntVector4): IntVector4 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        this.z = clamp(this.z, min.z, max.z)
        this.w = clamp(this.w, min.w, max.w)
        return this
    }
    override inline fun clampScalar(minVal: Int, maxVal: Int): IntVector4 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        this.z = clamp(this.z, minVal, maxVal)
        this.w = clamp(this.w, minVal, maxVal)
        return this
    }

    override inline fun negate(): IntVector4 {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
        this.w = -this.w
        return this
    }

    override inline infix fun dot(v: IntVector4) = this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w
    override inline fun lengthSq() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w
    override inline fun manhattanLength() = abs(this.x) + abs(this.y) + abs(this.z) + abs(this.w)
    override fun distanceToSquared(v: IntVector4): Int {
        val dx = this.x - v.x; val dy = this.y - v.y; val dz = this.z - v.z; val dw = this.w - v.w
        return dx * dx + dy * dy + dz * dz + dw * dw
    }
    override fun manhattanDistanceTo(v: IntVector4) =
        abs(this.x - v.x) + abs(this.y - v.y) + abs(this.z - v.z) + abs(this.w - v.w)

    inline fun toVector() = Vector4(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), this.w.toFloat())
    inline fun toUIntVector() = UIntVector4(this.x.toUInt(), this.y.toUInt(), this.z.toUInt(), this.w.toUInt())
    inline fun toBoolVector() = BoolVector4(this.x != 0, this.y != 0, this.z != 0, this.w != 0)

    override fun fromArray(array: IntArray, offset: Int): IntVector4 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        this.w = array[offset + 3]
        return this
    }
    override inline fun toArray(): IntArray = intArrayOf(this.x, this.y, this.z, this.w)
    override fun toArray(array: IntArray, offset: Int): IntArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        array[offset + 3] = this.w
        return array
    }

    companion object { val pool = Pool { IntVector4() } }
}
