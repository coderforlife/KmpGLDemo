@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class IntVector3(
    @JvmField var x: Int = 0,
    @JvmField var y: Int = 0,
    @JvmField var z: Int = 0
): IntVector<IntVector3> {
    override val size: Int get() = 3

    inline fun set(x: Int = this.x,
                   y: Int = this.y,
                   z: Int = this.z): IntVector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override inline fun setScalar(scalar: Int): IntVector3 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        return this
    }

    inline fun setX(x: Int): IntVector3 {
        this.x = x
        return this
    }
    inline fun setY(y: Int): IntVector3 {
        this.y = y
        return this
    }
    inline fun setZ(z: Int): IntVector3 {
        this.z = z
        return this
    }

    override inline fun setComponent(index: Int, value: Int): IntVector3 {
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
    override operator fun set(key: String, value: Int) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Int =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Int) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Int =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = IntVector3(this.x, this.y, this.z)
    override inline fun copy(other: IntVector3) = this.set(other.x, other.y, other.z)

    override inline fun add(v: IntVector3): IntVector3 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        return this
    }
    override inline fun addScalar(s: Int): IntVector3 {
        this.x += s
        this.y += s
        this.z += s
        return this
    }
    override inline fun addVectors(a: IntVector3, b: IntVector3): IntVector3 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        return this
    }
    override inline fun addScaledVector(v: IntVector3, s: Int): IntVector3 {
        this.x += v.x * s
        this.y += v.y * s
        this.z += v.z * s
        return this
    }

    override inline fun sub(v: IntVector3): IntVector3 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        return this
    }
    override inline fun subScalar(s: Int): IntVector3 {
        this.x -= s
        this.y -= s
        this.z -= s
        return this
    }
    override inline fun subVectors(a: IntVector3, b: IntVector3): IntVector3 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        return this
    }

    override inline fun multiply(v: IntVector3): IntVector3 {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        return this
    }
    override inline fun multiplyScalar(scalar: Int): IntVector3 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        return this
    }
    override inline fun multiplyScalar(scalar: Float): IntVector3 {
        this.x = (this.x * scalar).toInt()
        this.y = (this.y * scalar).toInt()
        this.z = (this.z * scalar).toInt()
        return this
    }
    override inline fun multiplyVectors(a: IntVector3, b: IntVector3): IntVector3 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        return this
    }

    override inline fun divide(v: IntVector3): IntVector3 {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        return this
    }
    override inline fun divideScalar(scalar: Int): IntVector3 {
        this.x /= scalar
        this.y /= scalar
        this.z /= scalar
        return this
    }

    override inline fun min(v: IntVector3): IntVector3 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        this.z = min(this.z, v.z)
        return this
    }

    override inline fun max(v: IntVector3): IntVector3 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        this.z = max(this.z, v.z)
        return this
    }

    override inline fun clamp(min: IntVector3, max: IntVector3): IntVector3 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        this.z = clamp(this.z, min.z, max.z)
        return this
    }
    override inline fun clampScalar(minVal: Int, maxVal: Int): IntVector3 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        this.z = clamp(this.z, minVal, maxVal)
        return this
    }

    override inline fun negate(): IntVector3 {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
        return this
    }

    override inline infix fun dot(v: IntVector3) = this.x * v.x + this.y * v.y + this.z * v.z
    override inline fun lengthSq() = this.x * this.x + this.y * this.y + this.z * this.z
    override inline fun manhattanLength() = abs(this.x) + abs(this.y) + abs(this.z)

    inline infix fun cross(v: IntVector3): IntVector3 = this.crossVectors(this, v)
    inline fun crossVectors(a: IntVector3, b: IntVector3): IntVector3 {
        val ax = a.x; val ay = a.y; val az = a.z
        val bx = b.x; val by = b.y; val bz = b.z
        this.x = ay * bz - az * by
        this.y = az * bx - ax * bz
        this.z = ax * by - ay * bx
        return this
    }
    inline operator fun rem(v: IntVector3): IntVector3 = this.clone().cross(v)
    inline operator fun remAssign(v: IntVector3) { this.cross(v) }

    override inline fun distanceToSquared(v: IntVector3): Int {
        val dx = this.x - v.x; val dy = this.y - v.y; val dz = this.z - v.z
        return dx * dx + dy * dy + dz * dz
    }
    override inline fun manhattanDistanceTo(v: IntVector3): Int =
        abs(this.x - v.x) + abs(this.y - v.y) + abs(this.z - v.z)

    inline fun toVector() = Vector3(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
    inline fun toUIntVector() = UIntVector3(this.x.toUInt(), this.y.toUInt(), this.z.toUInt())
    inline fun toBoolVector() = BoolVector3(this.x != 0, this.y != 0, this.z != 0)

    override fun fromArray(array: IntArray, offset: Int): IntVector3 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        return this
    }
    override inline fun toArray(): IntArray = intArrayOf(this.x, this.y, this.z)
    override fun toArray(array: IntArray, offset: Int): IntArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        return array
    }

    companion object { val pool = Pool { IntVector3() } }
}
