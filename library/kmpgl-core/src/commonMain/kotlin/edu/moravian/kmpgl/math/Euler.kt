@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "ObjectPropertyName", "PropertyName", "FunctionName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class Euler(
    @JvmField var _x: Float = 0f,
    @JvmField var _y: Float = 0f,
    @JvmField var _z: Float = 0f,
    @JvmField var _order: RotationOrder = DefaultOrder
) {

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

    var order: RotationOrder
        inline get() = this._order
        inline set(value) {
            this._order = value
            this._onChangeCallback?.invoke()
        }



    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Float) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    operator fun get(key: String): Float =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }
    operator fun set(key: Char, value: Float) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    operator fun get(key: Char): Float =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }

    inline fun set(x: Float = this._x,
                   y: Float = this._y,
                   z: Float = this._z,
                   order: RotationOrder = this._order): Euler {
        this._x = x
        this._y = y
        this._z = z
        this._order = order
        this._onChangeCallback?.invoke()
        return this
    }

    fun clone() = Euler(this._x, this._y, this._z, this._order)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Euler) = set(other._x, other._y, other._z, other._order)

    fun setFromRotationMatrix(m: Matrix4, order: RotationOrder = this._order,
                              update: Boolean = true): Euler {
        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)
        val te = m.elements
        val m11 = te[0]; val m12 = te[4]; val m13 = te[8]
        val m21 = te[1]; val m22 = te[5]; val m23 = te[9]
        val m31 = te[2]; val m32 = te[6]; val m33 = te[10]
        when (order) {
            RotationOrder.XYZ -> {
                this._y = asin(clamp(m13, -1f, 1f))
                if (abs(m13) < 0.9999999f) {
                    this._x = atan2(-m23, m33)
                    this._z = atan2(-m12, m11)
                } else {
                    this._x = atan2(m32, m22)
                    this._z = 0f
                }
            }
            RotationOrder.YXZ -> {
                this._x = asin(-clamp(m23, -1f, 1f))
                if (abs(m23) < 0.9999999f) {
                    this._y = atan2(m13, m33)
                    this._z = atan2(m21, m22)
                } else {
                    this._y = atan2(- m31, m11)
                    this._z = 0f
                }
            }
            RotationOrder.ZXY -> {
                this._x = asin(clamp(m32, -1f, 1f))
                if (abs(m32) < 0.9999999f) {
                    this._y = atan2(- m31, m33)
                    this._z = atan2(- m12, m22)
                } else {
                    this._y = 0f
                    this._z = atan2(m21, m11)
                }
            }
            RotationOrder.ZYX -> {
                this._y = asin(-clamp(m31, -1f, 1f))
                if (abs(m31) < 0.9999999f) {
                    this._x = atan2(m32, m33)
                    this._z = atan2(m21, m11)
                } else {
                    this._x = 0f
                    this._z = atan2(- m12, m22)
                }
            }

            RotationOrder.YZX -> {
                this._z = asin(clamp(m21, -1f, 1f))
                if (abs(m21) < 0.9999999f) {
                    this._x = atan2(- m23, m22)
                    this._y = atan2(- m31, m11)
                } else {
                    this._x = 0f
                    this._y = atan2(m13, m33)
                }
            }
            RotationOrder.XZY -> {
                this._z = asin(- clamp(m12, -1f, 1f))
                if (abs(m12) < 0.9999999f) {
                    this._x = atan2(m32, m22)
                    this._y = atan2(m13, m11)
                } else {
                    this._x = atan2(- m23, m33)
                    this._y = 0f
                }
            }
        }
        this._order = order
        if (update) this._onChangeCallback?.invoke()
        return this
    }

    fun setFromQuaternion(q: Quaternion, order: RotationOrder, update: Boolean = true) = Matrix4.pool { m ->
        this.setFromRotationMatrix(m.makeRotationFromQuaternion(q), order, update)
    }

    inline fun setFromVector3(v: Vector3, order: RotationOrder = this._order) =
        this.set(v.x, v.y, v.z, order)
    inline fun set(v: Vector3, order: RotationOrder = this._order) = this.setFromVector3(v, order)

    fun reorder(newOrder: RotationOrder) =
        // WARNING: this discards revolution information -bhouston
        Quaternion.pool { q -> this.setFromQuaternion(q.setFromEuler(this), newOrder) }

    inline fun fromArray(array: FloatArray, offset: Int = 0): Euler {
        this._x = array[offset + 0]
        this._y = array[offset + 1]
        this._z = array[offset + 2]
        // TODO: if (array.size > offset + 3) this._order = array[offset + 3]
        this._onChangeCallback?.invoke()
        return this
    }
    inline fun toArray(): FloatArray = floatArrayOf(this._x, this._y, this._z, 0f) // TODO: this._order
    inline fun toArray(array: FloatArray, offset: Int = 0): FloatArray {
        array[offset + 0] = this._x
        array[offset + 1] = this._y
        array[offset + 2] = this._z
        // TODO: array[offset + 3] = this._order
        return array
    }

    fun _onChange(callback: () -> Unit): Euler {
        this._onChangeCallback = callback
        return this
    }
    var _onChangeCallback: (() -> Unit)? = null
        private set

    enum class RotationOrder { XYZ, YZX, ZXY, XZY, YXZ, ZYX }

    companion object {
        val DefaultOrder = RotationOrder.XYZ
        val pool = Pool { Euler() }
    }
}
