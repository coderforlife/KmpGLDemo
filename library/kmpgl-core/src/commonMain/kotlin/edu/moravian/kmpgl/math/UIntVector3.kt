@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.*

data class UIntVector3(
    var x: UInt = 0u,
    var y: UInt = 0u,
    var z: UInt = 0u
): UIntVector<UIntVector3> {
    override val size: Int get() = 3

    inline fun set(x: UInt = this.x,
                   y: UInt = this.y,
                   z: UInt = this.z): UIntVector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override inline fun setScalar(scalar: UInt): UIntVector3 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        return this
    }

    inline fun setX(x: UInt): UIntVector3 {
        this.x = x
        return this
    }
    inline fun setY(y: UInt): UIntVector3 {
        this.y = y
        return this
    }
    inline fun setZ(z: UInt): UIntVector3 {
        this.z = z
        return this
    }

    override inline fun setComponent(index: Int, value: UInt): UIntVector3 {
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
    override operator fun set(key: String, value: UInt) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): UInt =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: UInt) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): UInt =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = UIntVector3(this.x, this.y, this.z)
    override inline fun copy(other: UIntVector3) = this.set(other.x, other.y, other.z)

    override inline fun add(v: UIntVector3): UIntVector3 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        return this
    }
    override inline fun addScalar(s: UInt): UIntVector3 {
        this.x += s
        this.y += s
        this.z += s
        return this
    }
    override inline fun addVectors(a: UIntVector3, b: UIntVector3): UIntVector3 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        return this
    }
    override inline fun addScaledVector(v: UIntVector3, s: UInt): UIntVector3 {
        this.x += v.x * s
        this.y += v.y * s
        this.z += v.z * s
        return this
    }

    override inline fun sub(v: UIntVector3): UIntVector3 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        return this
    }
    override inline fun subScalar(s: UInt): UIntVector3 {
        this.x -= s
        this.y -= s
        this.z -= s
        return this
    }
    override inline fun subVectors(a: UIntVector3, b: UIntVector3): UIntVector3 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        return this
    }

    override inline fun multiply(v: UIntVector3): UIntVector3 {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        return this
    }
    override inline fun multiplyScalar(scalar: UInt): UIntVector3 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        return this
    }
    override inline fun multiplyScalar(scalar: Float): UIntVector3 {
        this.x = (this.x.toDouble() * scalar).toUInt()
        this.y = (this.y.toDouble() * scalar).toUInt()
        this.z = (this.z.toDouble() * scalar).toUInt()
        return this
    }
    override inline fun multiplyVectors(a: UIntVector3, b: UIntVector3): UIntVector3 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        return this
    }

    override inline fun divide(v: UIntVector3): UIntVector3 {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        return this
    }
    override inline fun divideScalar(scalar: UInt): UIntVector3 {
        this.x /= scalar
        this.y /= scalar
        this.z /= scalar
        return this
    }

    override inline fun min(v: UIntVector3): UIntVector3 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        this.z = min(this.z, v.z)
        return this
    }

    override inline fun max(v: UIntVector3): UIntVector3 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        this.z = max(this.z, v.z)
        return this
    }

    override inline fun clamp(min: UIntVector3, max: UIntVector3): UIntVector3 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        this.z = clamp(this.z, min.z, max.z)
        return this
    }
    override inline fun clampScalar(minVal: UInt, maxVal: UInt): UIntVector3 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        this.z = clamp(this.z, minVal, maxVal)
        return this
    }

    override inline infix fun dot(v: UIntVector3) = this.x * v.x + this.y * v.y + this.z * v.z
    override inline fun lengthSq() = this.x * this.x + this.y * this.y + this.z * this.z
    override inline fun manhattanLength() = this.x + this.y + this.z

    inline infix fun cross(v: UIntVector3): UIntVector3 = this.crossVectors(this, v)
    inline fun crossVectors(a: UIntVector3, b: UIntVector3): UIntVector3 {
        val ax = a.x; val ay = a.y; val az = a.z
        val bx = b.x; val by = b.y; val bz = b.z
        this.x = ay * bz - az * by
        this.y = az * bx - ax * bz
        this.z = ax * by - ay * bx
        return this
    }
    inline operator fun rem(v: UIntVector3): UIntVector3 = this.clone().cross(v)
    inline operator fun remAssign(v: UIntVector3) { this.cross(v) }

    override inline fun distanceToSquared(v: UIntVector3): UInt {
        val dx = this.x - v.x; val dy = this.y - v.y; val dz = this.z - v.z
        return dx * dx + dy * dy + dz * dz
    }
    override inline fun manhattanDistanceTo(v: UIntVector3): UInt =
        absDiff(this.x, v.x) + absDiff(this.y, v.y) + absDiff(this.z, v.z)

    inline fun toVector() = Vector3(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())
    inline fun toIntVector() = IntVector3(this.x.toInt(), this.y.toInt(), this.z.toInt())
    inline fun toBoolVector() = BoolVector3(this.x != 0u, this.y != 0u, this.z != 0u)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun fromArray(array: UIntArray, offset: Int): UIntVector3 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        return this
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    override inline fun toArray() = uintArrayOf(this.x, this.y, this.z)
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toArray(array: UIntArray, offset: Int): UIntArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        return array
    }

    companion object { val pool = Pool { UIntVector3() } }
}
