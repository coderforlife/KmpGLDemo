@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

sealed interface Matrix<M: Matrix<M>> {
    val size: Int
    val elems: FloatArray // every subclass has .elements which is more direct access, this one may be just slightly slower
    fun identity(): M
    fun clone(): M
    fun copy(other: M): M
    fun multiplyMatrices(a: M, b: M): M
    fun multiplyScalar(s: Float): M
    fun determinant(): Float
    fun transpose(): M
    fun invert(): M
    fun fromArray(array: FloatArray, offset: Int = 0): M
    fun toArray(): FloatArray
    fun toArray(array: FloatArray, offset: Int = 0): FloatArray
}

inline operator fun <M: Matrix<M>> M.get(i: Int, j: Int) = this.elems[i*size + j]
inline operator fun <M: Matrix<M>> M.set(i: Int, j: Int, x: Float) { this.elems[i*size + j] = x }
inline operator fun <M: Matrix<M>> M.get(i: Int) = this.elems[i]
inline operator fun <M: Matrix<M>> M.set(i: Int, x: Float) { this.elems[i] = x }
inline fun <M: Matrix<M>> M.multiply(m: M) = this.multiplyMatrices(this, m)
inline fun <M: Matrix<M>> M.premultiply(m: M) = this.multiplyMatrices(m, this)
inline operator fun <M: Matrix<M>> M.times(m: M): M = clone().multiply(m)
inline operator fun <M: Matrix<M>> M.timesAssign(m: M) { multiply(m) }
inline fun <M: Matrix<M>> M.multiply(s: Float) = multiplyScalar(s)
operator fun <M: Matrix<M>> M.times(s: Float) = clone().multiplyScalar(s)
operator fun <M: Matrix<M>> M.timesAssign(s: Float) { multiplyScalar(s) }
