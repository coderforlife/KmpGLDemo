@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "UnusedDataClassCopyResult", "CopyWithoutNamedArguments", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField

data class Box2(
    @JvmField val min: Vector2 = Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    @JvmField val max: Vector2 = Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
) {

    inline fun set(min: Vector2 = this.min,
                   max: Vector2 = this.max): Box2 {
        this.min.copy(min)
        this.max.copy(max)
        return this
    }

    inline fun setFromPoints(points: Array<Vector2>): Box2 {
        this.makeEmpty()
        points.forEach { this.expandByPoint(it) }
        return this
    }
    inline fun setFromPoints(points: Iterable<Vector2>): Box2 {
        this.makeEmpty()
        points.forEach { this.expandByPoint(it) }
        return this
    }
    inline fun setFromPoints(points: Sequence<Vector2>): Box2 {
        this.makeEmpty()
        points.forEach { this.expandByPoint(it) }
        return this
    }

    fun setFromCenterAndSize(center: Vector2, size: Float): Box2 {
        Vector2.pool { v ->
            val halfSize = v.copy(size).multiplyScalar(0.5f)
            this.min.copy(center).sub(halfSize)
            this.max.copy(center).add(halfSize)
        }
        return this
    }

    fun clone() = Box2().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Box2) = set(other.min, other.max)

    inline fun makeEmpty(): Box2 {
        this.min.x = Float.POSITIVE_INFINITY
        this.min.y = Float.POSITIVE_INFINITY
        this.max.x = Float.NEGATIVE_INFINITY
        this.max.y = Float.NEGATIVE_INFINITY
        return this
    }
    inline fun isEmpty() =
        // this is a more robust check for empty than (volume <= 0) because volume can get positive with two negative axes
        (this.max.x < this.min.x) || (this.max.y < this.min.y)

    inline fun getCenter(target: Vector2 = Vector2()) =
        if (this.isEmpty()) target.set(0f, 0f) else target.addVectors(this.min, this.max).multiplyScalar(0.5f)

    inline fun getSize(target: Vector2 = Vector2()) =
        if (this.isEmpty()) target.set(0f, 0f) else target.subVectors(this.max, this.min)

    inline fun expandByPoint(point: Vector2): Box2 {
        this.min.min(point)
        this.max.max(point)
        return this
    }
    inline fun expandByVector(vector: Vector2): Box2 {
        this.min.sub(vector)
        this.max.add(vector)
        return this
    }
    inline fun expandByScalar(scalar: Float): Box2 {
        this.min.addScalar(-scalar)
        this.max.addScalar(scalar)
        return this
    }

    inline fun containsPoint(point: Vector2) =
        point.x >= this.min.x && point.x <= this.max.x &&
        point.y >= this.min.y && point.y <= this.max.y

    inline fun containsBox(box: Box2) =
        this.min.x <= box.min.x && box.max.x <= this.max.x &&
        this.min.y <= box.min.y && box.max.y <= this.max.y

    inline fun getParameter(point: Vector2, target: Vector2 = Vector2()) =
    // This can potentially have a divide by zero if the box
        // has a size dimension of 0.
        target.set(
            (point.x - this.min.x) / (this.max.x - this.min.x),
            (point.y - this.min.y) / (this.max.y - this.min.y)
        )

    inline fun intersectsBox(box: Box2) =
        // using 6 splitting planes to rule out intersections.
        box.max.x >= this.min.x && box.min.x <= this.max.x &&
        box.max.y >= this.min.y && box.min.y <= this.max.y


    inline fun clampPoint(point: Vector2, target: Vector2 = Vector2()) =
        target.copy(point).clamp(this.min, this.max)

    fun distanceToPoint(point: Vector2) =
        Vector2.pool { v -> v.copy(point).clamp(this.min, this.max).sub(point).length() }

    inline fun intersect(box: Box2): Box2 {
        this.min.max(box.min)
        this.max.min(box.max)
        // ensure that if there is no overlap, the result is fully empty, not slightly empty with non-inf/+inf values that will cause subsequence intersects to erroneously return valid values.
        return if (this.isEmpty()) this.makeEmpty() else this
    }

    inline fun union(box: Box2): Box2 {
        this.min.min(box.min)
        this.max.max(box.max)
        return this
    }

    inline fun translate(offset: Vector2): Box2 {
        this.min.add(offset)
        this.max.add(offset)
        return this
    }

    companion object { val pool = Pool { Box2() } }
}
