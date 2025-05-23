@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UnusedDataClassCopyResult",
    "ObjectPropertyName", "LocalVariableName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class Frustum(
    @JvmField val planes: Array<Plane> = Array(6) { Plane() }
) {
    constructor(p0: Plane = Plane(), p1: Plane = Plane(), p2: Plane = Plane(),
                p3: Plane = Plane(), p4: Plane = Plane(), p5: Plane = Plane()) :
            this(arrayOf(p0, p1, p2, p3, p4, p5))

    inline fun set(p0: Plane = this.planes[0],
                   p1: Plane = this.planes[1],
                   p2: Plane = this.planes[2],
                   p3: Plane = this.planes[3],
                   p4: Plane = this.planes[4],
                   p5: Plane = this.planes[5]): Frustum {
        val planes = this.planes
        planes[0].copy(p0)
        planes[1].copy(p1)
        planes[2].copy(p2)
        planes[3].copy(p3)
        planes[4].copy(p4)
        planes[5].copy(p5)
        return this
    }

    fun clone() = Frustum().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Frustum): Frustum {
        this.planes.forEachIndexed { i, plane -> plane.copy(other.planes[i]) }
        return this
    }

    fun setFromProjectionMatrix(m: Matrix4): Frustum {
        val planes = this.planes
        val me = m.elements
        val me0 = me[0]; val me1 = me[1]; val me2 = me[2]; val me3 = me[3]
        val me4 = me[4]; val me5 = me[5]; val me6 = me[6]; val me7 = me[7]
        val me8 = me[8]; val me9 = me[9]; val me10 = me[10]; val me11 = me[11]
        val me12 = me[12]; val me13 = me[13]; val me14 = me[14]; val me15 = me[15]
        planes[0].setComponents(me3 - me0, me7 - me4, me11 - me8, me15 - me12).normalize()
        planes[1].setComponents(me3 + me0, me7 + me4, me11 + me8, me15 + me12).normalize()
        planes[2].setComponents(me3 + me1, me7 + me5, me11 + me9, me15 + me13).normalize()
        planes[3].setComponents(me3 - me1, me7 - me5, me11 - me9, me15 - me13).normalize()
        planes[4].setComponents(me3 - me2, me7 - me6, me11 - me10, me15 - me14).normalize()
        planes[5].setComponents(me3 + me2, me7 + me6, me11 + me10, me15 + me14).normalize()
        return this
    }

    fun intersectsSphere(sphere: Sphere): Boolean {
        val center = sphere.center
        val negRadius = -sphere.radius
        return this.planes.all { it.distanceToPoint(center) >= negRadius }
    }

    fun intersectsBox(box: Box3): Boolean =
        Vector3.pool { v ->
            for (plane in this.planes) {
                // corner at max distance
                v.x = if (plane.normal.x > 0) box.max.x else box.min.x
                v.y = if (plane.normal.y > 0) box.max.y else box.min.y
                v.z = if (plane.normal.z > 0) box.max.z else box.min.z
                if (plane.distanceToPoint(v) < 0) { return false }
            }
            true
        }

    fun containsPoint(point: Vector3): Boolean =
        this.planes.all { it.distanceToPoint(point) >= 0f }

    override fun equals(other: Any?): Boolean =
        (this === other) || (other != null && other is Frustum && planes.contentEquals(other.planes))
    override fun hashCode(): Int = planes.contentHashCode()
}
