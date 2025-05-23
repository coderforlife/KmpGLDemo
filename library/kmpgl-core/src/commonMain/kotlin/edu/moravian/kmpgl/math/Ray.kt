@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "UnusedDataClassCopyResult", "CopyWithoutNamedArguments",
    "ObjectPropertyName", "LocalVariableName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class Ray(
    @JvmField val origin: Vector3 = Vector3(),
    @JvmField val direction: Vector3 = Vector3(0f, 0f, -1f)
) {
    inline fun set(origin: Vector3 = this.origin,
                   direction: Vector3 = this.direction): Ray {
        this.origin.copy(origin)
        this.direction.copy(direction)
        return this
    }

    fun clone() = Ray().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Ray) = this.set(other.origin, other.direction)

    inline fun at(t: Float, target: Vector3 = Vector3()) =
        target.copy(this.direction).multiplyScalar(t).add(this.origin)
    inline operator fun get(t: Float) = this.at(t)

    inline fun lookAt(v: Vector3): Ray {
        this.direction.copy(v).sub(this.origin).normalize()
        return this
    }

    inline fun recast(t: Float): Ray {
        this.at(t, this.origin)
        return this
    }

    fun closestPointToPoint(point: Vector3, target: Vector3 = Vector3()): Vector3 {
        target.subVectors(point, this.origin)
        val directionDistance = target.dot(this.direction)
        return if (directionDistance < 0f) {
            target.copy(this.origin)
        } else {
            target.copy(this.direction).multiplyScalar(directionDistance).add(this.origin)
        }
    }

    inline fun distanceToPoint(point: Vector3) =
        sqrt(this.distanceSqToPoint(point))

    fun distanceSqToPoint(point: Vector3): Float =
        Vector3.pool { v ->
            val directionDistance = v.subVectors(point, this.origin).dot(this.direction)
            // point behind the ray
            if (directionDistance < 0f) {
                this.origin.distanceToSquared(point)
            } else {
                v.copy(this.direction).multiplyScalar(directionDistance).add(this.origin).distanceToSquared(point)
            }
        }

    fun distanceSqToSegment(v0: Vector3, v1: Vector3,
                            optionalPointOnRay: Vector3? = null,
                            optionalPointOnSegment: Vector3? = null): Float =
        // from https://github.com/pmjoniak/GeometricTools/blob/master/GTEngine/Include/Mathematics/GteDistRaySegment.h
        // It returns the min distance between the ray and the segment
        // defined by v0 and v1
        // It can also set two optional targets :
        // - The closest point on the ray
        // - The closest point on the segment
        Vector3.pool { _segCenter, _segDir, _diff ->
            _segCenter.copy(v0).add(v1).multiplyScalar(0.5f)
            _segDir.copy(v1).sub(v0).normalize()
            _diff.copy(this.origin).sub(_segCenter)

            val segExtent = v0.distanceTo(v1) * 0.5f
            val a01 = - this.direction.dot(_segDir)
            val b0 = _diff.dot(this.direction)
            val b1 = - _diff.dot(_segDir)
            val c = _diff.lengthSq()
            val det = abs(1f - a01 * a01)
            var s0: Float
            var s1: Float
            val sqrDist: Float

            if (det > 0f) {
                // The ray and segment are not parallel.
                s0 = a01 * b1 - b0
                s1 = a01 * b0 - b1
                val extDet = segExtent * det
                if (s0 >= 0f) {
                    if (s1 >= - extDet) {
                        if (s1 <= extDet) {
                            // region 0
                            // Minimum at interior points of ray and segment.
                            val invDet = 1f / det
                            s0 *= invDet
                            s1 *= invDet
                            sqrDist = s0 * (s0 + a01 * s1 + 2f * b0) + s1 * (a01 * s0 + s1 + 2f * b1) + c
                        } else {
                            // region 1
                            s1 = segExtent
                            s0 = max(0f, -(a01 * s1 + b0))
                            sqrDist = - s0 * s0 + s1 * (s1 + 2f * b1) + c
                        }
                    } else {
                        // region 5
                        s1 = -segExtent
                        s0 = max(0f, -(a01 * s1 + b0))
                        sqrDist = - s0 * s0 + s1 * (s1 + 2f * b1) + c
                    }
                } else {
                    if (s1 <= -extDet) {
                        // region 4
                        s0 = max(0f, -(-a01 * segExtent + b0))
                        s1 = if (s0 > 0f) -segExtent else clamp(-b1, -segExtent, segExtent)
                        sqrDist = - s0 * s0 + s1 * (s1 + 2f * b1) + c
                    } else if (s1 <= extDet) {
                        // region 3
                        s0 = 0f
                        s1 = clamp(-b1, -segExtent, segExtent)
                        sqrDist = s1 * (s1 + 2 * b1) + c
                    } else {
                        // region 2
                        s0 = max(0f, - (a01 * segExtent + b0))
                        s1 = if (s0 > 0f) segExtent else clamp(-b1, -segExtent, segExtent)
                        sqrDist = - s0 * s0 + s1 * (s1 + 2f * b1) + c
                    }
                }
            } else {
                // Ray and segment are parallel.
                s1 = if (a01 > 0f) -segExtent else segExtent
                s0 = max(0f, - (a01 * s1 + b0))
                sqrDist = - s0 * s0 + s1 * (s1 + 2f * b1) + c
            }
            optionalPointOnRay?.copy(this.direction)?.multiplyScalar(s0)?.add(this.origin)
            optionalPointOnSegment?.copy(_segDir)?.multiplyScalar(s1)?.add(_segCenter)
            sqrDist
        }

    fun intersectSphere(sphere: Sphere, target: Vector3): Vector3? =
        Vector3.pool { v ->
            v.subVectors(sphere.center, this.origin)
            val tca = v.dot(this.direction)
            val d2 = v.dot(v) - tca * tca
            val radius2 = sphere.radius * sphere.radius
            if (d2 > radius2) return null

            val thc = sqrt(radius2 - d2)

            // t0 = first intersect point - entrance on front of sphere
            val t0 = tca - thc

            // t1 = second intersect point - exit point on back of sphere
            val t1 = tca + thc

            // test to see if both t0 and t1 are behind the ray - if so, return null
            if (t0 < 0 && t1 < 0) null

            // test to see if t0 is behind the ray:
            // if it is, the ray is inside the sphere, so return the second exit point scaled by t1,
            // in order to always return an intersect point that is in front of the ray.
            else if (t0 < 0) this.at(t1, target)

            // else t0 is in front of the ray, so return the first collision point scaled by t0
            else this.at(t0, target)
        }

    inline fun intersectsSphere(sphere: Sphere) =
        this.distanceSqToPoint(sphere.center) <= (sphere.radius * sphere.radius)

    fun distanceToPlane(plane: Plane): Float? {
        val denominator = plane.normal.dot(this.direction)
        if (denominator == 0f) {
            // line is coplanar, return origin
            // Null is preferable to undefined since undefined means.... it is undefined
            return if (plane.distanceToPoint(this.origin) == 0f) 0f else null
        }
        val t = -(this.origin.dot(plane.normal) + plane.constant) / denominator
        // Return if the ray never intersects the plane
        return if (t >= 0f) t else null
    }

    inline fun intersectPlane(plane: Plane, target: Vector3 = Vector3()): Vector3? {
        val t = this.distanceToPlane(plane)
        return if (t === null) null else this.at(t, target)
    }

    fun intersectsPlane(plane: Plane): Boolean {
        // check if the ray lies on the plane first
        val distToPoint = plane.distanceToPoint(this.origin)
        if (distToPoint == 0f) { return true }
        val denominator = plane.normal.dot(this.direction)
        // check if ray origin is behind the plane (and is pointing behind it)
        return denominator * distToPoint < 0f
    }

    fun intersectBox(box: Box3, target: Vector3 = Vector3()): Vector3? {
        var tmin: Float; var tmax: Float
        val tymin: Float; val tymax: Float; val tzmin: Float; val tzmax: Float
        val invdirx = 1f / this.direction.x
        val invdiry = 1f / this.direction.y
        val invdirz = 1f / this.direction.z
        val origin = this.origin
        if (invdirx >= 0f) {
            tmin = (box.min.x - origin.x) * invdirx
            tmax = (box.max.x - origin.x) * invdirx
        } else {
            tmin = (box.max.x - origin.x) * invdirx
            tmax = (box.min.x - origin.x) * invdirx
        }
        if (invdiry >= 0f) {
            tymin = (box.min.y - origin.y) * invdiry
            tymax = (box.max.y - origin.y) * invdiry
        } else {
            tymin = (box.max.y - origin.y) * invdiry
            tymax = (box.min.y - origin.y) * invdiry
        }
        if ((tmin > tymax) || (tymin > tmax)) return null
        if (tymin > tmin || tmin.isNaN()) tmin = tymin
        if (tymax < tmax || tmax.isNaN()) tmax = tymax
        if (invdirz >= 0f) {
            tzmin = (box.min.z - origin.z) * invdirz
            tzmax = (box.max.z - origin.z) * invdirz
        } else {
            tzmin = (box.max.z - origin.z) * invdirz
            tzmax = (box.min.z - origin.z) * invdirz
        }
        if ((tmin > tzmax) || (tzmin > tmax)) return null
        if (tzmin > tmin || tmin.isNaN()) tmin = tzmin
        if (tzmax < tmax || tmax.isNaN()) tmax = tzmax

        //return point closest to the ray (positive side)
        return if (tmax < 0f) null else this.at(if (tmin >= 0f) tmin else tmax, target)
    }

    fun intersectsBox(box: Box3) = Vector3.pool { v ->
        this.intersectBox(box, v) !== null
    }

    inline fun intersectTriangle(t: Triangle, backfaceCulling: Boolean = false,
                                 target: Vector3 = Vector3()): Vector3? =
        intersectTriangle(t.a, t.b, t.c, backfaceCulling, target)

    fun intersectTriangle(a: Vector3, b: Vector3, c: Vector3, backfaceCulling: Boolean = false,
                          target: Vector3 = Vector3()): Vector3? =
        // Compute the offset origin, edges, and normal.
        // from https://github.com/pmjoniak/GeometricTools/blob/master/GTEngine/Include/Mathematics/GteIntrRay3Triangle3.h
        Vector3.pool { _edge1, _edge2, _normal, _diff ->
            _edge1.subVectors(b, a)
            _edge2.subVectors(c, a)
            _normal.crossVectors(_edge1, _edge2)

            // Solve Q + t*D = b1*E1 + b2*E2 (Q = kDiff, D = ray direction,
            // E1 = kEdge1, E2 = kEdge2, N = Cross(E1,E2)) by
            //   |Dot(D,N)|*b1 = sign(Dot(D,N))*Dot(D,Cross(Q,E2))
            //   |Dot(D,N)|*b2 = sign(Dot(D,N))*Dot(D,Cross(E1,Q))
            //   |Dot(D,N)|*t = -sign(Dot(D,N))*Dot(Q,N)
            var DdN = this.direction.dot(_normal)
            if (DdN == 0f || DdN > 0f && backfaceCulling) { return null }
            val sign = if (DdN > 0f) 1 else { DdN = -DdN; -1 }

            // b1 < 0, no intersection
            _diff.subVectors(this.origin, a)
            val DdQxE2 = sign * this.direction.dot(_edge2.crossVectors(_diff, _edge2))
            if (DdQxE2 < 0f) { return null }

            // b2 < 0, no intersection
            val DdE1xQ = sign * this.direction.dot(_edge1.cross(_diff))
            if (DdE1xQ < 0f) { return null }

            // b1+b2 > 1, no intersection
            if (DdQxE2 + DdE1xQ > DdN) { return null }

            // Line intersects triangle, check if ray does.
            val QdN = -sign * _diff.dot(_normal)

            // t < 0, no intersection
            if (QdN < 0f) { return null }

            // Ray intersects triangle.
            this.at(QdN / DdN, target)
        }

    fun applyMatrix4(matrix4: Matrix4): Ray {
        this.origin.applyMatrix4(matrix4)
        this.direction.transformDirection(matrix4)
        return this
    }

    companion object { val pool = Pool { Ray() } }
}
