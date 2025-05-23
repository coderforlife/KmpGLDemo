@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*
import kotlin.random.Random

data class Vector4(
    @JvmField var x: Float = 0f,
    @JvmField var y: Float = 0f,
    @JvmField var z: Float = 0f,
    @JvmField var w: Float = 1f
): Vector<Vector4> {
    // NOTE: width/height properties moved to the IntVector4 class
    override val size: Int get() = 4

    inline fun set(x: Float = this.x,
                   y: Float = this.y,
                   z: Float = this.z,
                   w: Float = this.w): Vector4 {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    override inline fun setScalar(scalar: Float): Vector4 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        this.w = scalar
        return this
    }

    inline fun setX(x: Float): Vector4 {
        this.x = x
        return this
    }
    inline fun setY(y: Float): Vector4 {
        this.y = y
        return this
    }
    inline fun setZ(z: Float): Vector4 {
        this.z = z
        return this
    }
    inline fun setW(w: Float): Vector4 {
        this.w = w
        return this
    }

    override inline fun setComponent(index: Int, value: Float): Vector4 {
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
    override operator fun set(key: String, value: Float) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            "w" -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Float =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            "w" -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Float) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            'w' -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Float =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            'w' -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = Vector4(this.x, this.y, this.z, this.w)
    override inline fun copy(other: Vector4) = set(other.x, other.y, other.z, other.w)
    inline fun copy(v: Vector3) = set(v.x, v.y, v.z, 1f)

    override inline fun add(v: Vector4): Vector4 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        this.w += v.w
        return this
    }
    override inline fun addScalar(s: Float): Vector4 {
        this.x += s
        this.y += s
        this.z += s
        this.w += s
        return this
    }
    override inline fun addVectors(a: Vector4, b: Vector4): Vector4 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        this.w = a.w + b.w
        return this
    }
    override inline fun addScaledVector(v: Vector4, s: Float): Vector4 {
        this.x += v.x * s
        this.y += v.y * s
        this.z += v.z * s
        this.w += v.w * s
        return this
    }

    override inline fun sub(v: Vector4): Vector4 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        this.w -= v.w
        return this
    }
    override inline fun subScalar(s: Float): Vector4 {
        this.x -= s
        this.y -= s
        this.z -= s
        this.w -= s
        return this
    }
    override inline fun subVectors(a: Vector4, b: Vector4): Vector4 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        this.w = a.w - b.w
        return this
    }

    override inline fun multiply(v: Vector4): Vector4 {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        this.w *= v.w
        return this
    }
    override inline fun multiplyScalar(scalar: Float): Vector4 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        this.w *= scalar
        return this
    }
    override inline fun multiplyVectors(a: Vector4, b: Vector4): Vector4 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        this.w = a.w * b.w
        return this
    }

    fun applyMatrix4(m: Matrix4): Vector4 {
        val x = this.x; val y = this.y; val z = this.z; val w = this.w
        val e = m.elements
        this.x = e[0] * x + e[4] * y + e[8] * z + e[12] * w
        this.y = e[1] * x + e[5] * y + e[9] * z + e[13] * w
        this.z = e[2] * x + e[6] * y + e[10] * z + e[14] * w
        this.w = e[3] * x + e[7] * y + e[11] * z + e[15] * w
        return this
    }

    override inline fun divide(v: Vector4): Vector4 {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        this.w /= v.w
        return this
    }

    fun setAxisAngleFromQuaternion(q: Quaternion): Vector4 {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToAngle/index.htm
        // q is assumed to be normalized
        val w = 2 * acos(q.w)
        val s = sqrt(1f - q.w * q.w)
        return if (s < 0.0001f) this.set(1f, 0f, 0f, w) else this.set(q.x/s, q.y/s, q.z/s, w)
    }

    fun setAxisAngleFromRotationMatrix(m: Matrix4): Vector4 {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/index.htm
        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)
        val epsilon = 0.01f // margin to allow for rounding errors
        val epsilon2 = 0.1f // margin to distinguish between 0 and 180 degrees
        val te = m.elements
        val m11 = te[0]; val m12 = te[4]; val m13 = te[8]
        val m21 = te[1]; val m22 = te[5]; val m23 = te[9]
        val m31 = te[2]; val m32 = te[6]; val m33 = te[10]

        if ((abs(m12 - m21) < epsilon) &&
            (abs(m13 - m31) < epsilon) &&
            (abs(m23 - m32) < epsilon)) {

            // singularity found
            // first check for identity matrix which must have +1 for all terms
            // in leading diagonal and zero in other terms
            if ((abs(m12 + m21) < epsilon2) &&
                (abs(m13 + m31) < epsilon2) &&
                (abs(m23 + m32) < epsilon2) &&
                (abs(m11 + m22 + m33 - 3) < epsilon2)) {

                // this singularity is identity matrix so angle = 0
                return this.set(1f, 0f, 0f, 0f) // zero angle, arbitrary axis
            }

            val xx = 0.5f * (m11 + 1f)
            val yy = 0.5f * (m22 + 1f)
            val zz = 0.5f * (m33 + 1f)
            return if ((xx > yy) && (xx > zz)) {
                // m11 is the largest diagonal term
                if (xx < epsilon) {
                    this.set(0f, 0.707106781f, 0.707106781f, PI.toFloat())
                } else {
                    this.set(sqrt(xx), (m12+m21)/(4f*x), (m13+m31)/(4f*x), PI.toFloat())
                }
            } else if (yy > zz) {
                // m22 is the largest diagonal term
                if (yy < epsilon) {
                    this.set(0.707106781f, 0f, 0.707106781f, PI.toFloat())
                } else {
                    this.set((m12+m21)/(4f*y), sqrt(yy), (m23+m32)/(4f*y), PI.toFloat())
                }
            } else {
                // m33 is the largest diagonal term so base result on this
                if (zz < epsilon) {
                    this.set(0.707106781f, 0.707106781f, 0f, PI.toFloat())
                } else {
                    this.set((m13+m31)/(4f*z), (m23+m32)/(4f*z), sqrt(zz), PI.toFloat())
                }
            }
        }

        // as we have reached here there are no singularities so we can handle normally
        var s = sqrt((m32 - m23) * (m32 - m23) +
                     (m13 - m31) * (m13 - m31) +
                     (m21 - m12) * (m21 - m12)) // used to normalize
        // prevent divide by zero, should not happen if matrix is orthogonal and should be
        // caught by singularity test above, but I've left it in just in case
        if (abs(s) < 0.001f) s = 1f
        this.x = (m32 - m23) / s
        this.y = (m13 - m31) / s
        this.z = (m21 - m12) / s
        this.w = acos((m11 + m22 + m33 - 1f) / 2f)
        return this
    }

    override inline fun min(v: Vector4): Vector4 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        this.z = min(this.z, v.z)
        this.w = min(this.w, v.w)
        return this
    }

    override inline fun max(v: Vector4): Vector4 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        this.z = max(this.z, v.z)
        this.w = max(this.w, v.w)
        return this
    }

    override inline fun clamp(min: Vector4, max: Vector4): Vector4 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        this.z = clamp(this.z, min.z, max.z)
        this.w = clamp(this.w, min.w, max.w)
        return this
    }
    override inline fun clampScalar(minVal: Float, maxVal: Float): Vector4 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        this.z = clamp(this.z, minVal, maxVal)
        this.w = clamp(this.w, minVal, maxVal)
        return this
    }

    override inline fun floor(): Vector4 {
        this.x = floor(this.x)
        this.y = floor(this.y)
        this.z = floor(this.z)
        this.w = floor(this.w)
        return this
    }
    inline fun floorToInt(v: IntVector4 = IntVector4()): IntVector4 {
        v.x = this.x.toInt()
        v.y = this.y.toInt()
        v.z = this.z.toInt()
        v.w = this.w.toInt()
        return v
    }

    override inline fun ceil(): Vector4 {
        this.x = ceil(this.x)
        this.y = ceil(this.y)
        this.z = ceil(this.z)
        this.w = ceil(this.w)
        return this
    }
    inline fun ceilToInt(v: IntVector4 = IntVector4()): IntVector4 {
        v.x = ceil(this.x).toInt()
        v.y = ceil(this.y).toInt()
        v.z = ceil(this.z).toInt()
        v.w = ceil(this.w).toInt()
        return v
    }

    override inline fun round(): Vector4 {
        this.x = round(this.x)
        this.y = round(this.y)
        this.z = round(this.z)
        this.w = round(this.w)
        return this
    }
    inline fun roundToInt(v: IntVector4 = IntVector4()): IntVector4 {
        v.x = this.x.roundToInt()
        v.y = this.y.roundToInt()
        v.z = this.z.roundToInt()
        v.w = this.w.roundToInt()
        return v
    }

    override inline fun roundToZero(): Vector4 {
        this.x = if (this.x < 0f) ceil(this.x) else floor(this.x)
        this.y = if (this.y < 0f) ceil(this.y) else floor(this.y)
        this.z = if (this.z < 0f) ceil(this.z) else floor(this.z)
        this.w = if (this.w < 0f) ceil(this.w) else floor(this.w)
        return this
    }
    inline fun roundToZeroToInt(v: IntVector4 = IntVector4()): IntVector4 {
        v.x = (if (this.x < 0f) ceil(this.x) else floor(this.x)).toInt()
        v.y = (if (this.y < 0f) ceil(this.y) else floor(this.y)).toInt()
        v.z = (if (this.z < 0f) ceil(this.z) else floor(this.z)).toInt()
        v.w = (if (this.w < 0f) ceil(this.w) else floor(this.w)).toInt()
        return v
    }


    override inline fun negate(): Vector4 {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
        this.w = -this.w
        return this
    }

    override inline infix fun dot(v: Vector4) = this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w
    override inline fun lengthSq() = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w
    override inline fun manhattanLength() = abs(this.x) + abs(this.y) + abs(this.z) + abs(this.w)

    override inline fun lerp(v: Vector4, alpha: Float): Vector4 {
        this.x += (v.x - this.x) * alpha
        this.y += (v.y - this.y) * alpha
        this.z += (v.z - this.z) * alpha
        this.w += (v.w - this.w) * alpha
        return this
    }
    override inline fun lerpVectors(v1: Vector4, v2: Vector4, alpha: Float): Vector4 {
        this.x = v1.x + (v2.x - v1.x) * alpha
        this.y = v1.y + (v2.y - v1.y) * alpha
        this.z = v1.z + (v2.z - v1.z) * alpha
        this.w = v1.w + (v2.w - v1.w) * alpha
        return this
    }

    override inline fun distanceToSquared(v: Vector4): Float {
        val dx = this.x - v.x; val dy = this.y - v.y; val dz = this.z - v.z; val dw = this.w - v.w
        return dx * dx + dy * dy + dz * dz + dw * dw
    }
    override inline fun manhattanDistanceTo(v: Vector4): Float =
        abs(this.x - v.x) + abs(this.y - v.y) + abs(this.z - v.z) + abs(this.w - v.w)

    inline fun toIntVector() = IntVector4(this.x.toInt(), this.y.toInt(), this.z.toInt(), this.w.toInt())
    inline fun toUIntVector() = UIntVector4(this.x.toUInt(), this.y.toUInt(), this.z.toUInt(), this.w.toUInt())
    inline fun toBoolVector() = BoolVector4(this.x != 0f, this.y != 0f, this.z != 0f, this.w != 0f)

    override fun fromArray(array: FloatArray, offset: Int): Vector4 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        this.w = array[offset + 3]
        return this
    }
    override inline fun toArray(): FloatArray = floatArrayOf(this.x, this.y, this.z, this.w)
    override fun toArray(array: FloatArray, offset: Int): FloatArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        array[offset + 3] = this.w
        return array
    }

    override fun random(): Vector4 {
        this.x = Random.nextFloat()
        this.y = Random.nextFloat()
        this.z = Random.nextFloat()
        this.w = Random.nextFloat()
        return this
    }

    companion object {
        val pool = Pool { Vector4() }
        val ZERO = Vector4(0f, 0f, 0f, 0f)
        val ONE = Vector4(1f, 1f, 1f, 1f)
    }
}
