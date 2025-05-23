@file:Suppress("unused", "MemberVisibilityCanBePrivate", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class Matrix2(
    @JvmField val elements: FloatArray = floatArrayOf(1f, 0f, 0f, 1f)
): Matrix<Matrix2> {
    override val size: Int get() = 2
    override val elems: FloatArray get() = elements

    fun set(n11: Float, n12: Float,
            n21: Float, n22: Float): Matrix2 {
        val te = this.elements
        te[0] = n11; te[1] = n21
        te[2] = n12; te[3] = n22
        return this
    }

    override inline fun identity(): Matrix2 =
        this.set(
            1f, 0f,
            0f, 1f
        )

    override fun clone() = Matrix2().fromArray(this.elements)
    override inline fun copy(other: Matrix2) = fromArray(other.elements)

    override inline fun multiplyMatrices(a: Matrix2, b: Matrix2): Matrix2 {
        val ae = a.elements
        val be = b.elements
        val te = this.elements

        val a11 = ae[0]; val a12 = ae[2]
        val a21 = ae[1]; val a22 = ae[3]

        val b11 = be[0]; val b12 = be[2]
        val b21 = be[1]; val b22 = be[3]

        te[0] = a11 * b11 + a12 * b21
        te[2] = a11 * b12 + a12 * b22

        te[1] = a21 * b11 + a22 * b21
        te[3] = a21 * b12 + a22 * b22

        return this
    }

    override fun multiplyScalar(s: Float): Matrix2 {
        val te = this.elements
        te[0] *= s; te[2] *= s
        te[1] *= s; te[3] *= s
        return this
    }

    override fun determinant(): Float {
        val te = this.elements
        val a = te[0]; val b = te[1]
        val c = te[2]; val d = te[3]
        return a * d - b * c
    }

    override fun transpose(): Matrix2 {
        val m = this.elements
        val tmp = m[1]; m[1] = m[3]; m[3] = tmp
        return this
    }

    override fun invert(): Matrix2 {
        val te = this.elements
        val a = te[0]; val b = te[1]
        val c = te[2]; val d = te[3]

        val det = a * d - b * c
        if (det == 0f) return this.set(0f, 0f, 0f, 0f)
        val detInv = 1f / det

        te[0] = d * detInv
        te[1] = -b * detInv
        te[2] = -c * detInv
        te[3] = a * detInv
        return this
    }

    override fun fromArray(array: FloatArray, offset: Int): Matrix2 {
        array.copyInto(this.elements, 0, offset, offset + 4)
        return this
    }
    override fun toArray() = this.elements.copyOf()
    override fun toArray(array: FloatArray, offset: Int) = this.elements.copyInto(array, offset)

    override fun equals(other: Any?) =
        (this === other) or (other is Matrix2 && this.elements.contentEquals(other.elements))
    override fun hashCode() = this.elements.contentHashCode()
    override fun toString() = "Matrix2${this.elements.contentToString()}"

    companion object {
        val pool = Pool { Matrix2() }
        val IDENTITY = Matrix2()
    }
}
