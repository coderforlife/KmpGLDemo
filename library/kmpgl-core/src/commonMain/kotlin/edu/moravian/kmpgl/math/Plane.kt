@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UnusedDataClassCopyResult",
    "ObjectPropertyName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class Plane(
    @JvmField val normal: Vector3 = Vector3(1f, 0f, 0f),  // normal is assumed to be normalized
    @JvmField var constant: Float = 0f
) {
    inline fun set(normal: Vector3 = this.normal,
                   constant: Float = this.constant): Plane {
        this.normal.copy(normal)
        this.constant = constant
        return this
    }
    inline fun setComponents(x: Float = this.normal.x,
                             y: Float = this.normal.y,
                             z: Float = this.normal.z,
                             w: Float = this.constant): Plane {
        this.normal.set(x, y, z)
        this.constant = w
        return this
    }
    inline fun set(x: Float = this.normal.x,
                   y: Float = this.normal.y,
                   z: Float = this.normal.z,
                   w: Float = this.constant) = setComponents(x, y, z, w)

    inline fun setFromNormalAndCoplanarPoint(normal: Vector3, point: Vector3): Plane {
        this.normal.copy(normal)
        this.constant = -point.dot(this.normal)
        return this
    }

    fun setFromCoplanarPoints(a: Vector3, b: Vector3, c: Vector3): Plane {
        Vector3.pool {  v1, v2 ->
            val normal = v1.subVectors(c, b).cross(v2.subVectors(a, b)).normalize()
            // Q: should an error be thrown if normal is zero (e.g. degenerate plane)?
            this.setFromNormalAndCoplanarPoint(normal, a)
        }
        return this
    }

    fun clone() = Plane().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Plane) = this.set(other.normal, other.constant)

    fun normalize(): Plane {
        // Note: will lead to a divide by zero if the plane is invalid.
        val inverseNormalLength = 1f / this.normal.length()
        this.normal.multiplyScalar(inverseNormalLength)
        this.constant *= inverseNormalLength
        return this
    }

    inline fun negate(): Plane {
        this.constant *= -1
        this.normal.negate()
        return this
    }
    inline operator fun unaryMinus() = this.clone().negate()

    inline fun distanceToPoint(point: Vector3) = this.normal.dot(point) + this.constant
    inline fun distanceToSphere(sphere: Sphere) = this.distanceToPoint(sphere.center) - sphere.radius
    inline fun projectPoint(point: Vector3, target: Vector3 = Vector3()) =
        target.copy(this.normal).multiplyScalar(-this.distanceToPoint(point)).add(point)

    fun intersectLine(line: Line3, target: Vector3): Vector3? =
        Vector3.pool { v ->
            val direction = line.delta(v)
            val denominator = this.normal.dot(direction)
            if (denominator == 0f) {
                // line is coplanar, return origin
                if (this.distanceToPoint(line.start) == 0f)
                    return target.copy(line.start)
                // Unsure if this is the correct method to handle this case.
                return null
            }
            val t = -(line.start.dot(this.normal) + this.constant) / denominator
            if (t < 0 || t > 1) null else target.copy(direction).multiplyScalar(t).add(line.start)
        }

    inline fun intersectsLine(line: Line3): Boolean {
        // Note: this tests if a line intersects the plane, not whether it (or its end-points) are coplanar with it.
        val startSign = this.distanceToPoint(line.start)
        val endSign = this.distanceToPoint(line.end)
        return (startSign < 0 && endSign > 0) || (endSign < 0 && startSign > 0)
    }

    inline fun intersectsBox(box: Box3) = box.intersectsPlane(this)
    inline fun intersectsSphere(sphere: Sphere): Boolean = sphere.intersectsPlane(this)

    inline fun coplanarPoint(target: Vector3 = Vector3()) =
        target.copy(this.normal).multiplyScalar(-this.constant)

    fun applyMatrix4(matrix: Matrix4): Plane =
        Matrix3.pool { m -> applyMatrix4(matrix, m.getNormalMatrix(matrix)) }

    fun applyMatrix4(matrix: Matrix4, normalMatrix: Matrix3): Plane {
        val normal = this.normal.applyMatrix3(normalMatrix).normalize()
        Vector3.pool { v ->
            this.constant = -this.coplanarPoint(v).applyMatrix4(matrix).dot(normal)
        }
        return this
    }

    inline fun translate(offset: Vector3): Plane {
        this.constant -= offset.dot(this.normal)
        return this
    }
}
