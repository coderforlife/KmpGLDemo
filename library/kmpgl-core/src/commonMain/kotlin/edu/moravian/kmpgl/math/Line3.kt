@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UnusedDataClassCopyResult",
    "ObjectPropertyName", "LocalVariableName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class Line3(
    @JvmField val start: Vector3 = Vector3(),
    @JvmField val end: Vector3 = Vector3()
) {
    inline fun set(start: Vector3 = this.start,
                   end: Vector3 = this.end): Line3 {
        this.start.copy(start)
        this.end.copy(end)
        return this
    }

    fun clone() = Line3().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Line3) = set(other.start, other.end)

    inline fun getCenter(target: Vector3 = Vector3()) =
        target.addVectors(this.start, this.end).multiplyScalar(0.5f)

    inline fun delta(target: Vector3 = Vector3()) =
        target.subVectors(this.end, this.start)

    inline fun distanceSq() = this.start.distanceToSquared(this.end)
    inline fun distance() = this.start.distanceTo(this.end)

    inline fun at(t: Float, target: Vector3 = Vector3()) =
        this.delta(target).multiplyScalar(t).add(this.start)
    inline operator fun get(t: Float) = this.at(t)

    fun closestPointToPointParameter(point: Vector3, clampToLine: Boolean = false): Float =
        Vector3.pool { _startP, _startEnd ->
            _startP.subVectors(point, this.start)
            _startEnd.subVectors(this.end, this.start)
            val startEnd2 = _startEnd.dot(_startEnd)
            val startEnd_startP = _startEnd.dot(_startP)
            val t = startEnd_startP / startEnd2
            if (clampToLine) clamp(t, 0f, 1f) else t
        }

    inline fun closestPointToPoint(point: Vector3, clampToLine: Boolean = false, target: Vector3 = Vector3()) =
        this.delta(target).multiplyScalar(this.closestPointToPointParameter(point, clampToLine)).add(this.start)

    fun applyMatrix4(matrix: Matrix4): Line3 {
        this.start.applyMatrix4(matrix)
        this.end.applyMatrix4(matrix)
        return this
    }
}
