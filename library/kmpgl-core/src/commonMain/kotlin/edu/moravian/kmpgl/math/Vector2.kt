@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*
import kotlin.random.Random

data class Vector2(
    @JvmField var x: Float = 0f,
    @JvmField var y: Float = 0f
): Vector<Vector2> {
    // NOTE: width/height properties moved to the IntVector2 class
    override val size: Int get() = 2

    inline fun set(x: Float = this.x,
                   y: Float = this.y): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    override inline fun setScalar(scalar: Float): Vector2 {
        this.x = scalar
        this.y = scalar
        return this
    }

    inline fun setX(x: Float): Vector2 {
        this.x = x
        return this
    }
    inline fun setY(y: Float): Vector2 {
        this.y = y
        return this
    }

    override inline fun setComponent(index: Int, value: Float): Vector2 {
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
    override operator fun set(key: String, value: Float) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Float =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Float) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Float =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = Vector2(this.x, this.y)
    override inline fun copy(other: Vector2) = this.set(other.x, other.y)

    override inline fun add(v: Vector2): Vector2 {
        this.x += v.x
        this.y += v.y
        return this
    }
    override inline fun addScalar(s: Float): Vector2 {
        this.x += s
        this.y += s
        return this
    }
    override inline fun addVectors(a: Vector2, b: Vector2): Vector2 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        return this
    }
    override inline fun addScaledVector(v: Vector2, s: Float): Vector2 {
        this.x += v.x * s
        this.y += v.y * s
        return this
    }

    override inline fun sub(v: Vector2): Vector2 {
        this.x -= v.x
        this.y -= v.y
        return this
    }
    override inline fun subScalar(s: Float): Vector2 {
        this.x -= s
        this.y -= s
        return this
    }
    override inline fun subVectors(a: Vector2, b: Vector2): Vector2 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        return this
    }

    override inline fun multiply(v: Vector2): Vector2 {
        this.x *= v.x
        this.y *= v.y
        return this
    }
    override inline fun multiplyScalar(scalar: Float): Vector2 {
        this.x *= scalar
        this.y *= scalar
        return this
    }
    override inline fun multiplyVectors(a: Vector2, b: Vector2): Vector2 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        return this
    }

    fun applyMatrix3(m: Matrix3): Vector2 {
        val x = this.x; val y = this.y
        val e = m.elements
        this.x = e[0] * x + e[3] * y + e[6]
        this.y = e[1] * x + e[4] * y + e[7]
        return this
    }

    override inline fun divide(v: Vector2): Vector2 {
        this.x /= v.x
        this.y /= v.y
        return this
    }

    override inline fun min(v: Vector2): Vector2 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        return this
    }

    override inline fun max(v: Vector2): Vector2 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        return this
    }

    override inline fun clamp(min: Vector2, max: Vector2): Vector2 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        return this
    }
    override inline fun clampScalar(minVal: Float, maxVal: Float): Vector2 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        return this
    }

    override inline fun floor(): Vector2 {
        this.x = floor(this.x)
        this.y = floor(this.y)
        return this
    }
    inline fun floorToInt(v: IntVector2 = IntVector2()): IntVector2 {
        v.x = this.x.toInt()
        v.y = this.y.toInt()
        return v
    }

    override inline fun ceil(): Vector2 {
        this.x = ceil(this.x)
        this.y = ceil(this.y)
        return this
    }
    inline fun ceilToInt(v: IntVector2 = IntVector2()): IntVector2 {
        v.x = ceil(this.x).toInt()
        v.y = ceil(this.y).toInt()
        return v
    }

    override inline fun round(): Vector2 {
        this.x = round(this.x)
        this.y = round(this.y)
        return this
    }
    inline fun roundToInt(v: IntVector2 = IntVector2()): IntVector2 {
        v.x = this.x.roundToInt()
        v.y = this.y.roundToInt()
        return v
    }

    override inline fun roundToZero(): Vector2 {
        this.x = if (this.x < 0f) ceil(this.x) else floor(this.x)
        this.y = if (this.y < 0f) ceil(this.y) else floor(this.y)
        return this
    }
    inline fun roundToZeroToInt(v: IntVector2 = IntVector2()): IntVector2 {
        v.x = (if (this.x < 0f) ceil(this.x) else floor(this.x)).toInt()
        v.y = (if (this.y < 0f) ceil(this.y) else floor(this.y)).toInt()
        return v
    }

    override inline fun negate(): Vector2 {
        this.x = -this.x
        this.y = -this.y
        return this
    }

    override inline infix fun dot(v: Vector2) = this.x * v.x + this.y * v.y
    inline infix fun cross(v: Vector2) = this.x * v.y - this.y * v.x
    inline operator fun rem(v: Vector2) = this.cross(v)
    override inline fun lengthSq() = this.x * this.x + this.y * this.y
    override inline fun manhattanLength() = abs(this.x) + abs(this.y)

    override inline fun lerp(v: Vector2, alpha: Float): Vector2 {
        this.x += (v.x - this.x) * alpha
        this.y += (v.y - this.y) * alpha
        return this
    }

    override inline fun lerpVectors(v1: Vector2, v2: Vector2, alpha: Float): Vector2 {
        this.x = v1.x + (v2.x - v1.x) * alpha
        this.y = v1.y + (v2.y - v1.y) * alpha
        return this
    }

    // computes the angle in radians with respect to the positive x-axis
    inline fun angle() = atan2(-this.y, -this.x) + PI.toFloat()
    override inline fun distanceToSquared(v: Vector2): Float {
        val dx = this.x - v.x; val dy = this.y - v.y
        return dx * dx + dy * dy
    }
    override inline fun manhattanDistanceTo(v: Vector2): Float =
        abs(this.x - v.x) + abs(this.y - v.y)

    inline fun toIntVector() = IntVector2(this.x.toInt(), this.y.toInt())
    inline fun toUIntVector() = UIntVector2(this.x.toUInt(), this.y.toUInt())
    inline fun toBoolVector() = BoolVector2(this.x != 0f, this.y != 0f)

    override fun fromArray(array: FloatArray, offset: Int): Vector2 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        return this
    }
    override inline fun toArray(): FloatArray = floatArrayOf(this.x, this.y)
    override fun toArray(array: FloatArray, offset: Int): FloatArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        return array
    }

    fun rotateAround(center: Vector2, angle: Float): Vector2 {
        val c = cos(angle); val s = sin(angle)
        val x = this.x - center.x
        val y = this.y - center.y
        this.x = x * c - y * s + center.x
        this.y = x * s + y * c + center.y
        return this
    }

    override fun random(): Vector2 {
        this.x = Random.nextFloat()
        this.y = Random.nextFloat()
        return this
    }

    companion object {
        val pool = Pool { Vector2() }
        val ZERO = Vector2(0f, 0f)
        val ONE = Vector2(1f, 1f)
    }
}
