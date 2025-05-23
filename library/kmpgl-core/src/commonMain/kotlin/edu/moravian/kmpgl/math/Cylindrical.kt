@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.atan2
import kotlin.math.sqrt

data class Cylindrical(
    @JvmField var radius: Float = 1f, // distance from the origin to a point in the x-z plane
    @JvmField var theta: Float = 0f, // counterclockwise angle in the x-z plane measured in radians from the positive z-axis
    @JvmField var y: Float = 0f // height above the x-z plane
) {
    inline fun set(radius: Float = this.radius,
                   theta: Float = this.theta,
                   y: Float = this.y): Cylindrical {
        this.radius = radius
        this.theta = theta
        this.y = y
        return this
    }

    fun clone() = Cylindrical().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Cylindrical) = this.set(other.radius, other.theta, other.y)

    inline fun setFromVector3(v: Vector3): Cylindrical =
        this.setFromCartesianCoords(v.x, v.y, v.z)
    inline fun setFromCartesianCoords(v: Vector3): Cylindrical =
        this.setFromCartesianCoords(v.x, v.y, v.z)
    inline fun setFromCartesianCoords(x: Float, y: Float, z: Float): Cylindrical {
        this.radius = sqrt(x * x + z * z)
        this.theta = atan2(x, z)
        this.y = y
        return this
    }
}
