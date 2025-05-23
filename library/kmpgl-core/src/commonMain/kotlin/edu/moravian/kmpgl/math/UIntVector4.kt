@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.*

data class UIntVector4(
    var x: UInt = 0u,
    var y: UInt = 0u,
    var z: UInt = 0u,
    var w: UInt = 1u
): UIntVector<UIntVector4> {
    override val size: Int get() = 4

    var width: UInt
        get() = this.z
        set(value) { this.z = value }

    var height: UInt
        get() = this.w
        set(value) { this.w = value }

    inline fun set(x: UInt = this.x,
                   y: UInt = this.y,
                   z: UInt = this.z,
                   w: UInt = this.w): UIntVector4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override inline fun setScalar(scalar: UInt): UIntVector4 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        this.w = scalar
        return this
    }

    inline fun setX(x: UInt): UIntVector4 {
        this.x = x
        return this
    }
    inline fun setY(y: UInt): UIntVector4 {
        this.y = y
        return this
    }
    inline fun setZ(z: UInt): UIntVector4 {
        this.z = z
        return this
    }
    inline fun setW(w: UInt): UIntVector4 {
        this.w = w
        return this
    }

    override inline fun setComponent(index: Int, value: UInt): UIntVector4 {
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
    override operator fun set(key: String, value: UInt) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            "w" -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): UInt =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            "w" -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: UInt) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            'w' -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): UInt =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            'w' -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = UIntVector4(this.x, this.y, this.z, this.w)
    override inline fun copy(other: UIntVector4) = set(other.x, other.y, other.z, other.w)

    override inline fun add(v: UIntVector4): UIntVector4 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        this.w += v.w
        return this
    }
    override inline fun addScalar(s: UInt): UIntVector4 {
        this.x += s
        this.y += s
        this.z += s
        this.w += s
        return this
    }
    override inline fun addVectors(a: UIntVector4, b: UIntVector4): UIntVector4 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        this.w = a.w + b.w
        return this
    }
    override inline fun addScaledVector(v: UIntVector4, s: UInt): UIntVector4 {
        this.x += v.x * s
        this.y += v.y * s
        this.z += v.z * s
        this.w += v.w * s
        return this
    }

    override inline fun sub(v: UIntVector4): UIntVector4 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        this.w -= v.w
        return this
    }
    override inline fun subScalar(s: UInt): UIntVector4 {
        this.x -= s
        this.y -= s
        this.z -= s
        this.w -= s
        return this
    }
    override inline fun subVectors(a: UIntVector4, b: UIntVector4): UIntVector4 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        this.w = a.w - b.w
        return this
    }

    override inline fun multiply(v: UIntVector4): UIntVector4 {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        this.w *= v.w
        return this
    }
    override inline fun multiplyScalar(scalar: UInt): UIntVector4 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        this.w *= scalar
        return this
    }
    override inline fun multiplyScalar(scalar: Float): UIntVector4 {
        this.x = (this.x.toDouble() * scalar).toUInt()
        this.y = (this.y.toDouble() * scalar).toUInt()
        this.z = (this.z.toDouble() * scalar).toUInt()
        this.w = (this.w.toDouble() * scalar).toUInt()
        return this
    }
    override inline fun multiplyVectors(a: UIntVector4, b: UIntVector4): UIntVector4 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        this.w = a.w * b.w
        return this
    }

    override inline fun divide(v: UIntVector4): UIntVector4 {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        this.w /= v.w
        return this
    }
    override inline fun divideScalar(scalar: UInt): UIntVector4 {
        this.x /= scalar
        this.y /= scalar
        this.z /= scalar
        this.w /= scalar
        return this
    }

    override inline fun min(v: UIntVector4): UIntVector4 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        this.z = min(this.z, v.z)
        this.w = min(this.w, v.w)
        return this
    }

    override inline fun max(v: UIntVector4): UIntVector4 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        this.z = max(this.z, v.z)
        this.w = max(this.w, v.w)
        return this
    }

    override inline fun clamp(min: UIntVector4, max: UIntVector4): UIntVector4 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        this.z = clamp(this.z, min.z, max.z)
        this.w = clamp(this.w, min.w, max.w)
        return this
    }
    override inline fun clampScalar(minVal: UInt, maxVal: UInt): UIntVector4 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        this.z = clamp(this.z, minVal, maxVal)
        this.w = clamp(this.w, minVal, maxVal)
        return this
    }

    override inline infix fun dot(v: UIntVector4) = this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w
    override inline fun lengthSq() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w
    override inline fun manhattanLength() = this.x + this.y + this.z + this.w
    override fun distanceToSquared(v: UIntVector4): UInt {
        val dx = this.x - v.x; val dy = this.y - v.y; val dz = this.z - v.z; val dw = this.w - v.w
        return dx * dx + dy * dy + dz * dz + dw * dw
    }
    override fun manhattanDistanceTo(v: UIntVector4) =
        absDiff(this.x, v.x) + absDiff(this.y, v.y) + absDiff(this.z, v.z) + absDiff(this.w, v.w)

    inline fun toVector() = Vector4(this.x.toFloat(), this.y.toFloat(), this.z.toFloat(), this.w.toFloat())
    inline fun toIntVector() = IntVector4(this.x.toInt(), this.y.toInt(), this.z.toInt(), this.w.toInt())
    inline fun toBoolVector() = BoolVector4(this.x != 0u, this.y != 0u, this.z != 0u, this.w != 0u)

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun fromArray(array: UIntArray, offset: Int): UIntVector4 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        this.w = array[offset + 3]
        return this
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    override inline fun toArray() = uintArrayOf(this.x, this.y, this.z, this.w)
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun toArray(array: UIntArray, offset: Int): UIntArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        array[offset + 3] = this.w
        return array
    }

    companion object { val pool = Pool { UIntVector4() } }
}
