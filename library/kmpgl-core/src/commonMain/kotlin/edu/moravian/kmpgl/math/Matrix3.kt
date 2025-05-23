@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.cos
import kotlin.math.sin

data class Matrix3(
    @JvmField val elements: FloatArray = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
): Matrix<Matrix3> {
    override val size: Int get() = 3
    override val elems: FloatArray get() = elements

    fun set(n11: Float, n12: Float, n13: Float,
            n21: Float, n22: Float, n23: Float,
            n31: Float, n32: Float, n33: Float): Matrix3 {
        val te = this.elements
        te[0] = n11; te[1] = n21; te[2] = n31
        te[3] = n12; te[4] = n22; te[5] = n32
        te[6] = n13; te[7] = n23; te[8] = n33
        return this
    }

    override inline fun identity(): Matrix3 =
        this.set(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f,
       )

    override fun clone() = Matrix3().fromArray(this.elements)
    override inline fun copy(other: Matrix3) = fromArray(other.elements)

    inline fun setFromMatrix4(m: Matrix4): Matrix3 {
        val me = m.elements
        this.set(
            me[0], me[4], me[8],
            me[1], me[5], me[9],
            me[2], me[6], me[10]
       )
        return this
    }

    inline fun extractBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3): Matrix3 {
        xAxis.setFromMatrix3Column(this, 0)
        yAxis.setFromMatrix3Column(this, 1)
        zAxis.setFromMatrix3Column(this, 2)
        return this
    }

    inline fun makeBasis(xAxis: Vector3, yAxis: Vector3, zAxis: Vector3) =
        this.set(
            xAxis.x, yAxis.x, zAxis.x,
            xAxis.y, yAxis.y, zAxis.y,
            xAxis.z, yAxis.z, zAxis.z,
        )

    override inline fun multiplyMatrices(a: Matrix3, b: Matrix3): Matrix3 {
        val ae = a.elements
        val be = b.elements
        val te = this.elements

        val a11 = ae[0]; val a12 = ae[3]; val a13 = ae[6]
        val a21 = ae[1]; val a22 = ae[4]; val a23 = ae[7]
        val a31 = ae[2]; val a32 = ae[5]; val a33 = ae[8]

        val b11 = be[0]; val b12 = be[3]; val b13 = be[6]
        val b21 = be[1]; val b22 = be[4]; val b23 = be[7]
        val b31 = be[2]; val b32 = be[5]; val b33 = be[8]

        te[0] = a11 * b11 + a12 * b21 + a13 * b31
        te[3] = a11 * b12 + a12 * b22 + a13 * b32
        te[6] = a11 * b13 + a12 * b23 + a13 * b33

        te[1] = a21 * b11 + a22 * b21 + a23 * b31
        te[4] = a21 * b12 + a22 * b22 + a23 * b32
        te[7] = a21 * b13 + a22 * b23 + a23 * b33

        te[2] = a31 * b11 + a32 * b21 + a33 * b31
        te[5] = a31 * b12 + a32 * b22 + a33 * b32
        te[8] = a31 * b13 + a32 * b23 + a33 * b33

        return this
    }

    override fun multiplyScalar(s: Float): Matrix3 {
        val te = this.elements
        te[0] *= s; te[3] *= s; te[6] *= s
        te[1] *= s; te[4] *= s; te[7] *= s
        te[2] *= s; te[5] *= s; te[8] *= s
        return this
    }

    override fun determinant(): Float {
        val te = this.elements
        val a = te[0]; val b = te[1]; val c = te[2]
        val d = te[3]; val e = te[4]; val f = te[5]
        val g = te[6]; val h = te[7]; val i = te[8]
        return a * e * i - a * f * h - b * d * i + b * f * g + c * d * h - c * e * g
    }

    override fun transpose(): Matrix3 {
        var tmp: Float
        val m = this.elements
        tmp = m[1]; m[1] = m[3]; m[3] = tmp
        tmp = m[2]; m[2] = m[6]; m[6] = tmp
        tmp = m[5]; m[5] = m[7]; m[7] = tmp
        return this
    }

    fun transposeIntoArray(r: FloatArray): Matrix3 {
        val m = this.elements
        r[0] = m[0]
        r[1] = m[3]
        r[2] = m[6]
        r[3] = m[1]
        r[4] = m[4]
        r[5] = m[7]
        r[6] = m[2]
        r[7] = m[5]
        r[8] = m[8]
        return this
    }

    override fun invert(): Matrix3 {
        val te = this.elements

        val n11 = te[0]; val n21 = te[1]; val n31 = te[2]
        val n12 = te[3]; val n22 = te[4]; val n32 = te[5]
        val n13 = te[6]; val n23 = te[7]; val n33 = te[8]

        val t11 = n33 * n22 - n32 * n23
        val t12 = n32 * n13 - n33 * n12
        val t13 = n23 * n12 - n22 * n13

        val det = n11 * t11 + n21 * t12 + n31 * t13

        if (det == 0f) return this.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        val detInv = 1f / det

        te[0] = t11 * detInv
        te[1] = (n31 * n23 - n33 * n21) * detInv
        te[2] = (n32 * n21 - n31 * n22) * detInv

        te[3] = t12 * detInv
        te[4] = (n33 * n11 - n31 * n13) * detInv
        te[5] = (n31 * n12 - n32 * n11) * detInv

        te[6] = t13 * detInv
        te[7] = (n21 * n13 - n23 * n11) * detInv
        te[8] = (n22 * n11 - n21 * n12) * detInv

        return this
    }

    inline fun getNormalMatrix(matrix4: Matrix4): Matrix3 =
        this.setFromMatrix4(matrix4).invert().transpose()

    inline fun setUvTransform(tx: Float, ty: Float,
                              sx: Float, sy: Float,
                              rotation: Float,
                              cx: Float, cy: Float): Matrix3 {
        val c = cos(rotation)
        val s = sin(rotation)
        return this.set(
            sx * c, sx * s, - sx * (c * cx + s * cy) + cx + tx,
            - sy * s, sy * c, - sy * (- s * cx + c * cy) + cy + ty,
            0f, 0f, 1f
       )
    }

    fun scale(sx: Float, sy: Float) = pool { m -> this.premultiply(m.makeScale(sx, sy)) }
    fun rotate(theta: Float) = pool { m -> this.premultiply(m.makeRotation(-theta)) }
    fun translate(tx: Float, ty: Float) = pool { m -> this.premultiply(m.makeTranslation(tx, ty)) }

    // for 2D Transforms

    inline fun makeTranslation(x: Float, y: Float) = this.set(
            1f, 0f, x,
            0f, 1f, y,
            0f, 0f, 1f
       )
    inline fun makeRotation(theta: Float): Matrix3 {
        // counterclockwise
        val c = cos(theta)
        val s = sin(theta)
        return this.set(
            c, -s, 0f,
            s, c, 0f,
            0f, 0f, 1f

       )
    }
    inline fun makeScale(x: Float, y: Float) =
        this.set(
            x, 0f, 0f,
            0f, y, 0f,
            0f, 0f, 1f
       )

    override fun fromArray(array: FloatArray, offset: Int): Matrix3 {
        array.copyInto(this.elements, 0, offset, offset + 9)
        return this
    }
    override fun toArray() = this.elements.copyOf()
    override fun toArray(array: FloatArray, offset: Int) = this.elements.copyInto(array, offset)

    override fun equals(other: Any?) =
        (this === other) or (other is Matrix3 && this.elements.contentEquals(other.elements))
    override fun hashCode() = this.elements.contentHashCode()
    override fun toString() = "Matrix3${this.elements.contentToString()}"

    companion object {
        val pool = Pool { Matrix3() }
        val IDENTITY = Matrix3()
    }
}
