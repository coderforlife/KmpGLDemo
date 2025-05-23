@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UnusedDataClassCopyResult",
    "ObjectPropertyName", "FunctionName", "PropertyName", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*
import kotlin.random.Random

data class Quaternion(
    @JvmField var _x: Float = 0f,
    @JvmField var _y: Float = 0f,
    @JvmField var _z: Float = 0f,
    @JvmField var _w: Float = 1f
) {

    companion object {
        fun slerpFlat(dst: FloatArray, dstOffset: Int,
                      src0: FloatArray, srcOffset0: Int,
                      src1: FloatArray, srcOffset1: Int, t: Float) {
            // fuzz-free, array-based Quaternion SLERP operation
            var x0 = src0[srcOffset0 + 0]
            var y0 = src0[srcOffset0 + 1]
            var z0 = src0[srcOffset0 + 2]
            var w0 = src0[srcOffset0 + 3]

            val x1 = src1[srcOffset1 + 0]
            val y1 = src1[srcOffset1 + 1]
            val z1 = src1[srcOffset1 + 2]
            val w1 = src1[srcOffset1 + 3]

            if (t == 0f) {
                dst[dstOffset + 0] = x0
                dst[dstOffset + 1] = y0
                dst[dstOffset + 2] = z0
                dst[dstOffset + 3] = w0
                return
            }

            if (t == 1f) {
                dst[dstOffset + 0] = x1
                dst[dstOffset + 1] = y1
                dst[dstOffset + 2] = z1
                dst[dstOffset + 3] = w1
                return
            }

            var alpha = t
            if (w0 != w1 || x0 != x1 || y0 != y1 || z0 != z1) {
                var s = 1f - alpha
                val cos = x0 * x1 + y0 * y1 + z0 * z1 + w0 * w1
                val dir = if (cos >= 0f) 1f else -1f
                val sqrSin = 1f - cos * cos

                // Skip the Slerp for tiny steps to avoid numeric problems:
                if (sqrSin > EPSILON) {
                    val sin = sqrt(sqrSin)
                    val len = atan2(sin, cos * dir)
                    s = sin(s * len) / sin
                    alpha = sin(alpha * len) / sin
                }

                val tDir = alpha * dir
                x0 = x0 * s + x1 * tDir
                y0 = y0 * s + y1 * tDir
                z0 = z0 * s + z1 * tDir
                w0 = w0 * s + w1 * tDir

                // Normalize in case we just did a lerp:
                if (s == 1f - alpha) {
                    val f = 1f / sqrt(x0 * x0 + y0 * y0 + z0 * z0 + w0 * w0)
                    x0 *= f
                    y0 *= f
                    z0 *= f
                    w0 *= f
                }
            }
            dst[dstOffset] = x0
            dst[dstOffset + 1] = y0
            dst[dstOffset + 2] = z0
            dst[dstOffset + 3] = w0
        }

        fun multiplyQuaternionsFlat(dst: FloatArray, dstOffset: Int,
                                    src0: FloatArray, srcOffset0: Int,
                                    src1: FloatArray, srcOffset1: Int): FloatArray {
            val x0 = src0[srcOffset0]
            val y0 = src0[srcOffset0 + 1]
            val z0 = src0[srcOffset0 + 2]
            val w0 = src0[srcOffset0 + 3]
            val x1 = src1[srcOffset1]
            val y1 = src1[srcOffset1 + 1]
            val z1 = src1[srcOffset1 + 2]
            val w1 = src1[srcOffset1 + 3]
            dst[dstOffset] = x0 * w1 + w0 * x1 + y0 * z1 - z0 * y1
            dst[dstOffset + 1] = y0 * w1 + w0 * y1 + z0 * x1 - x0 * z1
            dst[dstOffset + 2] = z0 * w1 + w0 * z1 + x0 * y1 - y0 * x1
            dst[dstOffset + 3] = w0 * w1 - x0 * x1 - y0 * y1 - z0 * z1
            return dst
        }

        val pool = Pool { Quaternion() }
    }

    var x: Float
        inline get() = this._x
        inline set(value) {
            this._x = value
            this._onChangeCallback?.invoke()
        }

    var y: Float
        inline get() = this._y
        inline set(value) {
            this._y = value
            this._onChangeCallback?.invoke()
        }

    var z: Float
        inline get() = this._z
        inline set(value) {
            this._z = value
            this._onChangeCallback?.invoke()
        }

    var w: Float
        inline get() = this._w
        inline set(value) {
            this._w = value
            this._onChangeCallback?.invoke()
        }

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Float) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            "w" -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    operator fun get(key: String): Float =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            "w" -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }
    operator fun set(key: Char, value: Float) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            'w' -> this.w = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    operator fun get(key: Char): Float =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            'w' -> this.w
            else -> throw IllegalArgumentException("no key $key")
        }

    inline fun set(x: Float = this._x,
                   y: Float = this._y,
                   z: Float = this._z,
                   w: Float = this._w): Quaternion {
        this._x = x
        this._y = y
        this._z = z
        this._w = w
        this._onChangeCallback?.invoke()
        return this
    }

    fun clone() = Quaternion(this._x, this._y, this._z, this._w)
    inline fun copy(other: Quaternion) = set(other._x, other._y, other._z, other._w)

    fun setFromEuler(euler: Euler, update: Boolean = true): Quaternion {
        val x = euler._x; val y = euler._y; val z = euler._z; val order = euler._order

        // http://www.mathworks.com/matlabcentral/fileexchange/
        // 	20696-function-to-convert-between-dcm-euler-angles-quaternions-and-euler-vectors/
        //	content/SpinCalc.m

        val c1 = cos(x / 2f)
        val c2 = cos(y / 2f)
        val c3 = cos(z / 2f)

        val s1 = sin(x / 2f)
        val s2 = sin(y / 2f)
        val s3 = sin(z / 2f)

        when (order) {
            Euler.RotationOrder.XYZ -> {
                this._x = s1 * c2 * c3 + c1 * s2 * s3
                this._y = c1 * s2 * c3 - s1 * c2 * s3
                this._z = c1 * c2 * s3 + s1 * s2 * c3
                this._w = c1 * c2 * c3 - s1 * s2 * s3
            }

            Euler.RotationOrder.YXZ -> {
                this._x = s1 * c2 * c3 + c1 * s2 * s3
                this._y = c1 * s2 * c3 - s1 * c2 * s3
                this._z = c1 * c2 * s3 - s1 * s2 * c3
                this._w = c1 * c2 * c3 + s1 * s2 * s3
            }

            Euler.RotationOrder.ZXY -> {
                this._x = s1 * c2 * c3 - c1 * s2 * s3
                this._y = c1 * s2 * c3 + s1 * c2 * s3
                this._z = c1 * c2 * s3 + s1 * s2 * c3
                this._w = c1 * c2 * c3 - s1 * s2 * s3
            }

            Euler.RotationOrder.ZYX -> {
                this._x = s1 * c2 * c3 - c1 * s2 * s3
                this._y = c1 * s2 * c3 + s1 * c2 * s3
                this._z = c1 * c2 * s3 - s1 * s2 * c3
                this._w = c1 * c2 * c3 + s1 * s2 * s3
            }

            Euler.RotationOrder.YZX -> {
                this._x = s1 * c2 * c3 + c1 * s2 * s3
                this._y = c1 * s2 * c3 + s1 * c2 * s3
                this._z = c1 * c2 * s3 - s1 * s2 * c3
                this._w = c1 * c2 * c3 - s1 * s2 * s3
            }

            Euler.RotationOrder.XZY -> {
                this._x = s1 * c2 * c3 - c1 * s2 * s3
                this._y = c1 * s2 * c3 - s1 * c2 * s3
                this._z = c1 * c2 * s3 + s1 * s2 * c3
                this._w = c1 * c2 * c3 + s1 * s2 * s3
            }
        }
        if (!update) this._onChangeCallback?.invoke()
        return this
    }

    fun setFromAxisAngle(axis: Vector3, angle: Float): Quaternion {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToQuaternion/index.htm
        // assumes axis is normalized
        val halfAngle = angle / 2f; val s = sin(halfAngle)
        this._x = axis.x * s
        this._y = axis.y * s
        this._z = axis.z * s
        this._w = cos(halfAngle)
        this._onChangeCallback?.invoke()
        return this
    }

    fun setFromRotationMatrix(m: Matrix4): Quaternion {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)
        val te = m.elements
        val m11 = te[0]; val m12 = te[4]; val m13 = te[8]
        val m21 = te[1]; val m22 = te[5]; val m23 = te[9]
        val m31 = te[2]; val m32 = te[6]; val m33 = te[10]
        val trace = m11 + m22 + m33
        if (trace > 0f) {
            val s = 0.5f / sqrt(trace + 1f)
            this._w = 0.25f / s
            this._x = (m32 - m23) * s
            this._y = (m13 - m31) * s
            this._z = (m21 - m12) * s
        } else if (m11 > m22 && m11 > m33) {
            val s = 2f * sqrt(1f + m11 - m22 - m33)
            this._w = (m32 - m23) / s
            this._x = 0.25f * s
            this._y = (m12 + m21) / s
            this._z = (m13 + m31) / s
        } else if (m22 > m33) {
            val s = 2f * sqrt(1f + m22 - m11 - m33)
            this._w = (m13 - m31) / s
            this._x = (m12 + m21) / s
            this._y = 0.25f * s
            this._z = (m23 + m32) / s
        } else {
            val s = 2f * sqrt(1f + m33 - m11 - m22)
            this._w = (m21 - m12) / s
            this._x = (m13 + m31) / s
            this._y = (m23 + m32) / s
            this._z = 0.25f * s
        }
        this._onChangeCallback?.invoke()
        return this
    }

    fun setFromUnitVectors(vFrom: Vector3, vTo: Vector3): Quaternion {
        // assumes direction vectors vFrom and vTo are normalized
        val r = vFrom.dot(vTo) + 1f
        if (r < EPSILON) {
            // vFrom and vTo point in opposite directions
            if (abs(vFrom.x) > abs(vFrom.z)) {
                this._x = - vFrom.y
                this._y = vFrom.x
                this._z = 0f
                this._w = 0f
            } else {
                this._x = 0f
                this._y = - vFrom.z
                this._z = vFrom.y
                this._w = 0f
            }
        } else {
            // crossVectors(vFrom, vTo); // inlined to avoid cyclic dependency on Vector3
            this._x = vFrom.y * vTo.z - vFrom.z * vTo.y
            this._y = vFrom.z * vTo.x - vFrom.x * vTo.z
            this._z = vFrom.x * vTo.y - vFrom.y * vTo.x
            this._w = r
        }
        return this.normalize()
    }

    inline fun angleTo(q: Quaternion) = 2 * acos(abs(clamp(this.dot(q), -1f, 1f)))
    fun rotateTowards(q: Quaternion, step: Float): Quaternion {
        val angle = this.angleTo(q)
        if (angle == 0f) return this
        val t = min(1f, step / angle)
        this.slerp(q, t)
        return this
    }

    inline fun identity() = this.set(0f, 0f, 0f, 1f)
    inline fun invert() = this.conjugate() // quaternion is assumed to have unit length
    inline fun conjugate(): Quaternion {
        this._x *= -1f
        this._y *= -1f
        this._z *= -1f
        this._onChangeCallback?.invoke()
        return this
    }
    inline operator fun unaryMinus() = this.clone().invert()
    inline infix fun dot(v: Quaternion) =
        this._x * v._x + this._y * v._y + this._z * v._z + this._w * v._w
    inline fun lengthSq() =
        this._x * this._x + this._y * this._y + this._z * this._z + this._w * this._w
    inline fun length() =
        sqrt(this._x * this._x + this._y * this._y + this._z * this._z + this._w * this._w)
    fun normalize(): Quaternion {
        val l = this.length()
        if (l == 0f) {
            this._x = 0f
            this._y = 0f
            this._z = 0f
            this._w = 1f
        } else {
            val li = 1 / l
            this._x *= li
            this._y *= li
            this._z *= li
            this._w *= li
        }
        this._onChangeCallback?.invoke()
        return this
    }

    inline fun multiply(q: Quaternion) = this.multiplyQuaternions(this, q)
    inline fun premultiply(q: Quaternion) = this.multiplyQuaternions(q,this)
    fun multiplyQuaternions(a: Quaternion, b: Quaternion): Quaternion {
        // from http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/code/index.htm
        val qax = a._x; val qay = a._y; val qaz = a._z; val qaw = a._w
        val qbx = b._x; val qby = b._y; val qbz = b._z; val qbw = b._w
        this._x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby
        this._y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz
        this._z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx
        this._w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz
        this._onChangeCallback?.invoke()
        return this
    }

    fun slerp(qb: Quaternion, t: Float): Quaternion {
        if (t == 0f) return this
        if (t == 1f) return this.copy(qb)

        val x = this._x; val y = this._y; val z = this._z; val w = this._w

        // http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/

        var cosHalfTheta = w * qb._w + x * qb._x + y * qb._y + z * qb._z
        if (cosHalfTheta < 0f) {
            this._w = - qb._w
            this._x = - qb._x
            this._y = - qb._y
            this._z = - qb._z
            cosHalfTheta = -cosHalfTheta
        } else { this.copy(qb) }

        if (cosHalfTheta >= 1f) {
            this._w = w
            this._x = x
            this._y = y
            this._z = z
            return this
        }

        val sqrSinHalfTheta = 1f - cosHalfTheta * cosHalfTheta
        if (sqrSinHalfTheta <= EPSILON) {
            val s = 1f - t
            this._w = s * w + t * this._w
            this._x = s * x + t * this._x
            this._y = s * y + t * this._y
            this._z = s * z + t * this._z
            this.normalize()
            this._onChangeCallback?.invoke()
            return this
        }

        val sinHalfTheta = sqrt(sqrSinHalfTheta)
        val halfTheta = atan2(sinHalfTheta, cosHalfTheta)
        val ratioA = sin((1f - t) * halfTheta) / sinHalfTheta
        val ratioB = sin(t * halfTheta) / sinHalfTheta

        this._w = (w * ratioA + this._w * ratioB)
        this._x = (x * ratioA + this._x * ratioB)
        this._y = (y * ratioA + this._y * ratioB)
        this._z = (z * ratioA + this._z * ratioB)

        this._onChangeCallback?.invoke()
        return this
    }

    inline fun slerpQuaternions(qa: Quaternion, qb: Quaternion, t: Float) =
        this.copy(qa).slerp(qb, t)

    fun random(): Quaternion {
        // Derived from http://planning.cs.uiuc.edu/node198.html
        // Note, this source uses w, x, y, z ordering,
        // so we swap the order below.
        val u1 = Random.nextFloat()
        val sqrt1u1 = sqrt(1f - u1)
        val sqrtu1 = sqrt(u1)
        val u2 = PI_2 * Random.nextFloat()
        val u3 = PI_2 * Random.nextFloat()
        return this.set(
            sqrt1u1 * cos(u2),
            sqrtu1 * sin(u3),
            sqrtu1 * cos(u3),
            sqrt1u1 * sin(u2),
       )
    }

    inline fun fromArray(array: FloatArray, offset: Int = 0): Quaternion {
        this._x = array[offset + 0]
        this._y = array[offset + 1]
        this._z = array[offset + 2]
        this._w = array[offset + 3]
        this._onChangeCallback?.invoke()
        return this
    }
    inline fun toArray(): FloatArray = floatArrayOf(this._x, this._y, this._z, this._w)
    fun toArray(array: FloatArray, offset: Int = 0): FloatArray {
        array[offset + 0] = this._x
        array[offset + 1] = this._y
        array[offset + 2] = this._z
        array[offset + 3] = this._w
        return array
    }

    fun _onChange(callback: () -> Unit): Quaternion {
        this._onChangeCallback = callback
        return this
    }
    var _onChangeCallback: (() -> Unit)? = null
        private set
}
