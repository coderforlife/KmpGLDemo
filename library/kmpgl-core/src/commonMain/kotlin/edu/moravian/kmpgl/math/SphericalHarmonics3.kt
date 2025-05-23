@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UnusedDataClassCopyResult", "NOTHING_TO_INLINE",
    "OVERRIDE_BY_INLINE"
)

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

/**
 * Primary reference:
 *   https://graphics.stanford.edu/papers/envmap/envmap.pdf
 *
 * Secondary reference:
 *   https://www.ppsloan.org/publications/StupidSH36.pdf
 */

// 3-band SH defined by 9 coefficients

data class SphericalHarmonics3(
    @JvmField val coefficients: Array<Vector3> = Array(9) { Vector3() }
) {
    inline operator fun get(index: Int) = coefficients[index]
    inline operator fun set(index: Int, v: Vector3) { coefficients[index] = v }

    inline fun set(coefficients: Array<Vector3>): SphericalHarmonics3 {
        this.coefficients.zip(coefficients).forEach { (a, b) -> a.copy(b) }
        return this
    }
    inline fun set(coefficients: Iterable<Vector3>): SphericalHarmonics3 {
        this.coefficients.zip(coefficients).forEach { (a, b) -> a.copy(b) }
        return this
    }

    inline fun zero(): SphericalHarmonics3 {
        this.coefficients.forEach { it.set(0f, 0f, 0f) }
        return this
    }

    // get the radiance in the direction of the normal
    // target is a Vector3
    fun getAt(normal: Vector3, target: Vector3 = Vector3()): Vector3 {
        // normal is assumed to be unit length
        val x = normal.x; val y = normal.y; val z = normal.z
        val coeff = this.coefficients

        // band 0
        target.copy(coeff[0]).multiplyScalar(0.282095f)

        // band 1
        target.addScaledVector(coeff[1], 0.488603f * y)
        target.addScaledVector(coeff[2], 0.488603f * z)
        target.addScaledVector(coeff[3], 0.488603f * x)

        // band 2
        target.addScaledVector(coeff[4], 1.092548f * (x * y))
        target.addScaledVector(coeff[5], 1.092548f * (y * z))
        target.addScaledVector(coeff[6], 0.315392f * (3f * z * z - 1f))
        target.addScaledVector(coeff[7], 1.092548f * (x * z))
        target.addScaledVector(coeff[8], 0.546274f * (x * x - y * y))

        return target
    }

    // get the irradiance (radiance convolved with cosine lobe) in the direction of the normal
    // target is a Vector3
    // https://graphics.stanford.edu/papers/envmap/envmap.pdf
    fun getIrradianceAt(normal: Vector3, target: Vector3 = Vector3()): Vector3 {
        // normal is assumed to be unit length
        val x = normal.x; val y = normal.y; val z = normal.z
        val coeff = this.coefficients

        // band 0
        target.copy(coeff[0]).multiplyScalar(0.886227f) // π * 0.282095

        // band 1
        target.addScaledVector(coeff[1], 2.0f * 0.511664f * y) // (2 * π / 3) * 0.488603
        target.addScaledVector(coeff[2], 2.0f * 0.511664f * z)
        target.addScaledVector(coeff[3], 2.0f * 0.511664f * x)

        // band 2
        target.addScaledVector(coeff[4], 2.0f * 0.429043f * x * y) // (π / 4) * 1.092548
        target.addScaledVector(coeff[5], 2.0f * 0.429043f * y * z)
        target.addScaledVector(coeff[6], 0.743125f * z * z - 0.247708f) // (π / 4) * 0.315392 * 3
        target.addScaledVector(coeff[7], 2.0f * 0.429043f * x * z)
        target.addScaledVector(coeff[8], 0.429043f * (x * x - y * y)) // (π / 4) * 0.546274

        return target
    }

    inline fun add(sh: SphericalHarmonics3): SphericalHarmonics3 {
        this.coefficients.zip(sh.coefficients).forEach { (a, b) -> a.add(b) }
        return this
    }

    inline fun addScaledSH(sh: SphericalHarmonics3, s: Float): SphericalHarmonics3 {
        this.coefficients.zip(sh.coefficients).forEach { (a, b) -> a.addScaledVector(b, s) }
        return this
    }

    inline fun scale(s: Float): SphericalHarmonics3 {
        this.coefficients.forEach { it.multiplyScalar(s) }
        return this
    }

    inline fun lerp(sh: SphericalHarmonics3, alpha: Float): SphericalHarmonics3 {
        this.coefficients.zip(sh.coefficients).forEach { (a, b) -> a.lerp(b, alpha) }
        return this
    }

    override fun equals(other: Any?) =
        (this === other) || (other != null && other is SphericalHarmonics3 && coefficients.contentEquals(other.coefficients))
    override fun hashCode() = coefficients.contentHashCode()
    override fun toString() = "SphericalHarmonics3${this.coefficients.contentToString()}"

    fun clone() = SphericalHarmonics3().copy(this)
    inline fun copy(other: SphericalHarmonics3) = this.set(other.coefficients)

    inline fun fromArray(array: FloatArray, offset: Int = 0): SphericalHarmonics3 {
        this.coefficients.withIndex().forEach { (i, c) -> c.fromArray(array, offset+i*3) }
        return this
    }
    inline fun toArray(): FloatArray = toArray(FloatArray(27))
    fun toArray(array: FloatArray, offset: Int = 0): FloatArray {
        this.coefficients.withIndex().forEach { (i, c) -> c.toArray(array, offset+i*3) }
        return array
    }

    companion object {
        // evaluate the basis functions
        // shBasis is an Array[9]
        fun getBasisAt(normal: Vector3, shBasis: FloatArray) {
            // normal is assumed to be unit length
            val x = normal.x; val y = normal.y; val z = normal.z

            // band 0
            shBasis[0] = 0.282095f

            // band 1
            shBasis[1] = 0.488603f * y
            shBasis[2] = 0.488603f * z
            shBasis[3] = 0.488603f * x

            // band 2
            shBasis[4] = 1.092548f * x * y
            shBasis[5] = 1.092548f * y * z
            shBasis[6] = 0.315392f * (3f * z * z - 1f)
            shBasis[7] = 1.092548f * x * z
            shBasis[8] = 0.546274f * (x * x - y * y)
        }
    }
}