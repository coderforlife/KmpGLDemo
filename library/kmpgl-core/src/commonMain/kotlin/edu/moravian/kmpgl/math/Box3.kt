@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "UnusedDataClassCopyResult", "CopyWithoutNamedArguments", "LocalVariableName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class Box3(
    @JvmField val min: Vector3 = Vector3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    @JvmField val max: Vector3 = Vector3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
) {
    inline fun set(min: Vector3 = this.min,
                   max: Vector3 = this.max): Box3 {
        this.min.copy(min)
        this.max.copy(max)
        return this
    }

    fun setFromArray(array: FloatArray): Box3 {
        if (array.size < 3) { return this.makeEmpty() }
        var minX = array[0]
        var minY = array[1]
        var minZ = array[2]
        var maxX = minX
        var maxY = minY
        var maxZ = minZ
        for (i in 1 until array.size step 3) {
            val x = array[i]
            val y = array[i + 1]
            val z = array[i + 2]
            if (x < minX) minX = x else if (x > maxX) maxX = x
            if (y < minY) minY = y else if (y > maxY) maxY = y
            if (z < minZ) minZ = z else if (z > maxZ) maxZ = z
        }
        this.min.set(minX, minY, minZ)
        this.max.set(maxX, maxY, maxZ)
        return this
    }

    inline fun setFromPoints(points: Array<Vector3>): Box3 {
        this.makeEmpty()
        points.forEach { this.expandByPoint(it) }
        return this
    }
    inline fun setFromPoints(points: Iterable<Vector3>): Box3 {
        this.makeEmpty()
        points.forEach { this.expandByPoint(it) }
        return this
    }
    inline fun setFromPoints(points: Sequence<Vector3>): Box3 {
        this.makeEmpty()
        points.forEach { this.expandByPoint(it) }
        return this
    }

    fun setFromCenterAndSize(center: Vector3, size: Float): Box3 = Vector3.pool { v ->
        val halfSize = v.copy(size).multiplyScalar(0.5f)
        this.min.copy(center).sub(halfSize)
        this.max.copy(center).add(halfSize)
        this
    }

    fun clone() = Box3().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Box3) = set(other.min, other.max)

    inline fun makeEmpty(): Box3 {
        this.min.x = Float.POSITIVE_INFINITY
        this.min.y = Float.POSITIVE_INFINITY
        this.min.z = Float.POSITIVE_INFINITY
        this.max.x = Float.NEGATIVE_INFINITY
        this.max.y = Float.NEGATIVE_INFINITY
        this.max.z = Float.NEGATIVE_INFINITY
        return this
    }
    inline fun isEmpty() =
        // this is a more robust check for empty than (volume <= 0) because volume can get positive with two negative axes
        (this.max.x < this.min.x) || (this.max.y < this.min.y) || (this.max.z < this.min.z)

    inline fun getCenter(target: Vector3 = Vector3()) =
        if (this.isEmpty()) target.set(0f, 0f, 0f) else target.addVectors(this.min, this.max).multiplyScalar(0.5f)

    inline fun getSize(target: Vector3 = Vector3()) =
        if (this.isEmpty()) target.set(0f, 0f, 0f) else target.subVectors(this.max, this.min)

    inline val center get() = this.getCenter()
    inline val size get() = this.getSize()
    inline val height get() = this.max.x - this.min.x
    inline val width get() = this.max.y - this.min.y
    inline val depth get() = this.max.z - this.min.z

    inline fun expandByPoint(point: Vector3): Box3 {
        this.min.min(point)
        this.max.max(point)
        return this
    }
    inline fun expandByVector(vector: Vector3): Box3 {
        this.min.sub(vector)
        this.max.add(vector)
        return this
    }
    inline fun expandByScalar(scalar: Float): Box3 {
        this.min.addScalar(-scalar)
        this.max.addScalar(scalar)
        return this
    }

    inline fun containsPoint(point: Vector3) =
        point.x >= this.min.x && point.x <= this.max.x &&
        point.y >= this.min.y && point.y <= this.max.y &&
        point.z >= this.min.z && point.z <= this.max.z

    inline fun containsBox(box: Box3) =
        this.min.x <= box.min.x && box.max.x <= this.max.x &&
        this.min.y <= box.min.y && box.max.y <= this.max.y &&
        this.min.z <= box.min.z && box.max.z <= this.max.z

    inline fun getParameter(point: Vector3, target: Vector3 = Vector3()) =
        // This can potentially have a divide by zero if the box
        // has a size dimension of 0.
        target.set(
            (point.x - this.min.x) / (this.max.x - this.min.x),
            (point.y - this.min.y) / (this.max.y - this.min.y),
            (point.z - this.min.z) / (this.max.z - this.min.z)
        )

    inline fun intersectsBox(box: Box3) =
        // using 6 splitting planes to rule out intersections.
        box.max.x >= this.min.x && box.min.x <= this.max.x &&
        box.max.y >= this.min.y && box.min.y <= this.max.y &&
        box.max.z >= this.min.z && box.min.z <= this.max.z


    fun intersectsSphere(sphere: Sphere) = Vector3.pool { v ->
        // Find the point on the AABB closest to the sphere center.
        this.clampPoint(sphere.center, v)
            // If that point is inside the sphere, the AABB and sphere intersect.
            .distanceToSquared(sphere.center) <= (sphere.radius * sphere.radius)
    }

    fun intersectsPlane(plane: Plane): Boolean {
        // We compute the minimum and maximum dot product values. If those values
        // are on the same side (back or front) of the plane, then there is no intersection.
        var min: Float
        var max: Float
        if (plane.normal.x > 0f) {
            min = plane.normal.x * this.min.x
            max = plane.normal.x * this.max.x
        } else {
            min = plane.normal.x * this.max.x
            max = plane.normal.x * this.min.x
        }
        if (plane.normal.y > 0f) {
            min += plane.normal.y * this.min.y
            max += plane.normal.y * this.max.y
        } else {
            min += plane.normal.y * this.max.y
            max += plane.normal.y * this.min.y
        }
        if (plane.normal.z > 0f) {
            min += plane.normal.z * this.min.z
            max += plane.normal.z * this.max.z
        } else {
            min += plane.normal.z * this.max.z
            max += plane.normal.z * this.min.z
        }
        return -plane.constant in min..max
    }

    fun intersectsTriangle(triangle: Triangle): Boolean {
        if (this.isEmpty()) { return false }

        poolScoper {
            // compute box center and extents
            val _center = Vector3.pool(it)
            val _extents = Vector3.pool(it)
            this.getCenter(_center)
            _extents.subVectors(this.max, _center)

            // translate triangle to aabb origin
            val _v0 = Vector3.pool(it).subVectors(triangle.a, _center)
            val _v1 = Vector3.pool(it).subVectors(triangle.b, _center)
            val _v2 = Vector3.pool(it).subVectors(triangle.c, _center)

            // compute edge vectors for triangle
            val _f0 = Vector3.pool(it).subVectors(_v1, _v0)
            val _f1 = Vector3.pool(it).subVectors(_v2, _v1)
            val _f2 = Vector3.pool(it).subVectors(_v0, _v2)

            // test against axes that are given by cross product combinations of the edges of the triangle and the edges of the aabb
            // make an axis testing of each of the 3 sides of the aabb against each of the 3 sides of the triangle = 9 axis of separation
            // axis_ij = u_i x f_j (u0, u1, u2 = face normals of aabb = x,y,z axes vectors since aabb is axis aligned)
            val axes = floatArrayOf(
                0f, -_f0.z, _f0.y, 0f, -_f1.z, _f1.y, 0f, -_f2.z, _f2.y,
                _f0.z, 0f, -_f0.x, _f1.z, 0f, -_f1.x, _f2.z, 0f, -_f2.x,
                -_f0.y, _f0.x, 0f, -_f1.y, _f1.x, 0f, - _f2.y, _f2.x, 0f,

                // test 3 face normals from the aabb
                1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f,
            )
            val _testAxis = Vector3.pool(it)
            if (!satForAxes(axes, _testAxis, _v0, _v1, _v2, _extents)) { return false }

            // finally testing the face normal of the triangle
            // use already existing triangle edge vectors here
             return satForAxes(Vector3.pool(it).crossVectors(_f0, _f1).toArray(), _testAxis, _v0, _v1, _v2, _extents)
        }
    }

    inline fun clampPoint(point: Vector3, target: Vector3 = Vector3()) =
        target.copy(point).clamp(this.min, this.max)

    fun distanceToPoint(point: Vector3) =
        Vector3.pool { v -> v.copy(point).clamp(this.min, this.max).sub(point).length() }

    fun getBoundingSphere(target: Sphere = Sphere()): Sphere {
        this.getCenter(target.center)
        target.radius = Vector3.pool { v -> this.getSize(v).length() * 0.5f }
        return target
    }

    inline fun intersect(box: Box3): Box3 {
        this.min.max(box.min)
        this.max.min(box.max)
        // ensure that if there is no overlap, the result is fully empty, not slightly empty with non-inf/+inf values that will cause subsequence intersects to erroneously return valid values.
        return if (this.isEmpty()) this.makeEmpty() else this
    }

    inline fun union(box: Box3): Box3 {
        this.min.min(box.min)
        this.max.max(box.max)
        return this
    }

    fun applyMatrix4(matrix: Matrix4): Box3 {
        // transform of empty box is an empty box.
        if (this.isEmpty()) return this
        Vector3.pool(8) { _points ->
            // NOTE: I am using a binary pattern to specify all 2^3 combinations below
            _points[0].set(this.min.x, this.min.y, this.min.z).applyMatrix4(matrix) // 000
            _points[1].set(this.min.x, this.min.y, this.max.z).applyMatrix4(matrix) // 001
            _points[2].set(this.min.x, this.max.y, this.min.z).applyMatrix4(matrix) // 010
            _points[3].set(this.min.x, this.max.y, this.max.z).applyMatrix4(matrix) // 011
            _points[4].set(this.max.x, this.min.y, this.min.z).applyMatrix4(matrix) // 100
            _points[5].set(this.max.x, this.min.y, this.max.z).applyMatrix4(matrix) // 101
            _points[6].set(this.max.x, this.max.y, this.min.z).applyMatrix4(matrix) // 110
            _points[7].set(this.max.x, this.max.y, this.max.z).applyMatrix4(matrix) // 111
            this.setFromPoints(_points)
        }
        return this
    }

    inline fun translate(offset: Vector3): Box3 {
        this.min.add(offset)
        this.max.add(offset)
        return this
    }

    companion object { val pool = Pool { Box3() } }
}

private operator fun <T> ArrayList<T>.component6() = this[5]

private fun satForAxes(axes: FloatArray, testAxis: Vector3,
                       v0: Vector3, v1: Vector3, v2: Vector3, extents: Vector3): Boolean {
    for (i in 0 .. axes.size-3 step 3) {
        testAxis.fromArray(axes, i)
        // project the aabb onto the separating axis
        val r = extents.x * abs(testAxis.x) + extents.y * abs(testAxis.y) + extents.z * abs(testAxis.z)
        // project all 3 vertices of the triangle onto the separating axis
        val p0 = v0.dot(testAxis)
        val p1 = v1.dot(testAxis)
        val p2 = v2.dot(testAxis)
        // actual test, basically see if either of the most extreme of the triangle points intersects r
        if (max(-max(p0, p1, p2), min(p0, p1, p2)) > r) {
            // points of the projected triangle are outside the projected half-length of the aabb
            // the axis is separating and we can exit
            return false
        }
    }
    return true
}
