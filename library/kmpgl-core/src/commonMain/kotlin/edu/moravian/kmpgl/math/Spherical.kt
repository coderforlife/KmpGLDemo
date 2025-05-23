@file:Suppress("unused", "MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class Spherical(
    @JvmField var radius: Float = 1f,
    @JvmField var phi: Float = 0f,
    @JvmField var theta: Float = 0f
) {
    inline fun set(radius: Float = this.radius,
                   phi: Float = this.phi,
                   theta: Float = this.theta): Spherical {
        this.radius = radius
        this.phi = phi
        this.theta = theta
        return this
    }

    fun clone() = Spherical().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Spherical) = this.set(other.radius, other.phi, other.theta)

    // restrict phi to be between EPS and PI-EPS
    inline fun makeSafe(): Spherical {
        val EPS = 0.000001f
        this.phi = clamp(this.phi, EPS, PI.toFloat() - EPS)
        return this
    }

    inline fun setFromVector3(v: Vector3) = this.setFromCartesianCoords(v.x, v.y, v.z)
    inline fun setFromCartesianCoords(v: Vector3) = this.setFromCartesianCoords(v.x, v.y, v.z)
    fun setFromCartesianCoords(x: Float, y: Float, z: Float): Spherical {
        this.radius = sqrt(x * x + y * y + z * z)
        if (this.radius == 0f) {
            this.theta = 0f
            this.phi = 0f
        } else {
            this.theta = atan2(x, z)
            this.phi = acos(clamp(y / this.radius, -1f, 1f))
        }
        return this
    }
}
