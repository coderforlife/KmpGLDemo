@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UnusedDataClassCopyResult",
    "ObjectPropertyName", "LocalVariableName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class Sphere(
    @JvmField val center: Vector3 = Vector3(),
    @JvmField var radius: Float = -1f
) {
    inline fun set(center: Vector3 = this.center,
                   radius: Float = this.radius): Sphere {
        this.center.copy(center)
        this.radius = radius
        return this
    }
    inline fun set(centerX: Float = this.center.x,
                   centerY: Float = this.center.y,
                   centerZ: Float = this.center.z,
                   radius: Float = this.radius): Sphere {
        this.center.set(centerX, centerY, centerZ)
        this.radius = radius
        return this
    }

    fun setFromPoints(points: Array<Vector3>, optionalCenter: Vector3? = null): Sphere {
        val center = this.center
        if (optionalCenter !== null) {
            center.copy(optionalCenter)
        } else {
            Box3.pool { b -> b.setFromPoints(points).getCenter(center) }
        }
        this.radius = sqrt(points.maxOf { center.distanceToSquared(it) })
        return this
    }

    fun clone() = Sphere().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Sphere) = this.set(other.center, other.radius)

    inline fun isEmpty() = (this.radius < 0f)
    inline fun makeEmpty() = this.set(0f, 0f, 0f, -1f)

    inline fun containsPoint(point: Vector3) =
        (point.distanceToSquared(this.center) <= (this.radius * this.radius))

    inline fun distanceToPoint(point: Vector3) = (point.distanceTo(this.center) - this.radius)

    inline fun intersectsSphere(sphere: Sphere): Boolean {
        val radiusSum = this.radius + sphere.radius
        return sphere.center.distanceToSquared(this.center) <= (radiusSum * radiusSum)
    }

    inline fun intersectsBox(box: Box3) = box.intersectsSphere(this)
    inline fun intersectsPlane(plane: Plane) =
        abs(plane.distanceToPoint(this.center)) <= this.radius

    fun clampPoint(point: Vector3, target: Vector3 = Vector3()): Vector3 {
        val deltaLengthSq = this.center.distanceToSquared(point)
        target.copy(point)
        if (deltaLengthSq > (this.radius * this.radius)) {
            target.sub(this.center).normalize().multiplyScalar(this.radius).add(this.center)
        }
        return target
    }

    fun getBoundingBox(target: Box3 = Box3()): Box3 {
        if (this.isEmpty()) {
            // Empty sphere produces empty bounding box
            target.makeEmpty()
            return target
        }
        target.set(this.center, this.center)
        target.expandByScalar(this.radius)
        return target
    }

    inline fun applyMatrix4(matrix: Matrix4): Sphere {
        this.center.applyMatrix4(matrix)
        this.radius *= matrix.getMaxScaleOnAxis()
        return this
    }

    inline fun translate(offset: Vector3): Sphere {
        this.center.add(offset)
        return this
    }

    fun expandByPoint(point: Vector3): Sphere {
        if (this.isEmpty()) {
            this.center.copy(point)
            this.radius = 0f
            return this
        }
        Vector3.pool { v ->
            v.subVectors(point, this.center)
            val lengthSq = v.lengthSq()
            if (lengthSq > (this.radius * this.radius)) {
                // calculate the minimal sphere
                val length = sqrt(lengthSq)
                val delta = (length - this.radius) * 0.5f
                this.center.addScaledVector(v, delta / length)
                this.radius += delta
            }
        }
        return this
    }

    fun union(sphere: Sphere): Sphere {
        if (sphere.isEmpty()) {
            return this
        }
        if (this.isEmpty()) {
            this.copy(sphere)
            return this
        }
        if (this.center == sphere.center) {
            this.radius = max(this.radius, sphere.radius)
        } else {
            Vector3.pool { _v1, _v2 ->
                _v2.subVectors(sphere.center, this.center).setLength(sphere.radius)
                this.expandByPoint(_v1.copy(sphere.center).add(_v2))
                this.expandByPoint(_v1.copy(sphere.center).sub(_v2))
            }
        }
        return this
    }

    companion object { val pool = Pool { Sphere() } }
}
