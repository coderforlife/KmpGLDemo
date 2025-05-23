@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "UnusedDataClassCopyResult", "CopyWithoutNamedArguments", "LocalVariableName",
    "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*

data class Triangle(
    @JvmField val a: Vector3 = Vector3(),
    @JvmField val b: Vector3 = Vector3(),
    @JvmField val c: Vector3 = Vector3()
) {
    companion object {
        fun getNormal(a: Vector3, b: Vector3, c: Vector3, target: Vector3 = Vector3()): Vector3 {
            target.subVectors(c, b)
            Vector3.pool { v -> target.cross(v.subVectors(a, b)) }
            val targetLengthSq = target.lengthSq()
            return if (targetLengthSq > 0) target.multiplyScalar(1f / sqrt(targetLengthSq)) else target.set(0f, 0f, 0f)
        }

        // static/instance method to calculate barycentric coordinates
        // based on: http://www.blackpawn.com/texts/pointinpoly/default.html
        fun getBarycoord(point: Vector3, a: Vector3, b: Vector3, c: Vector3, target: Vector3 = Vector3()) = Vector3.pool { v0, v1, v2 ->
            v0.subVectors(c, a)
            v1.subVectors(b, a)
            v2.subVectors(point, a)
            val dot00 = v0.dot(v0)
            val dot01 = v0.dot(v1)
            val dot02 = v0.dot(v2)
            val dot11 = v1.dot(v1)
            val dot12 = v1.dot(v2)
            val denom = (dot00 * dot11 - dot01 * dot01)
            // collinear or singular triangle
            if (denom == 0f) {
                // arbitrary location outside of triangle?
                // not sure if this is the best idea, maybe should be returning undefined
                return target.set(-2f, -1f, -1f)
            }
            val invDenom = 1f / denom
            val u = (dot11 * dot02 - dot01 * dot12) * invDenom
            val v = (dot00 * dot12 - dot01 * dot02) * invDenom
            // barycentric coordinates must always sum to 1
            target.set(1f - u - v, v, u)
        }

        fun containsPoint(point: Vector3, a: Vector3, b: Vector3, c: Vector3) = Vector3.pool { v ->
            this.getBarycoord(point, a, b, c, v)
            (v.x >= 0f) && (v.y >= 0f) && ((v.x + v.y) <= 1f)
        }

        fun getUV(point: Vector3, p1: Vector3, p2: Vector3, p3: Vector3,
                  uv1: Vector2, uv2: Vector2, uv3: Vector2, target: Vector2 = Vector2()) = Vector3.pool { v ->
            this.getBarycoord(point, p1, p2, p3, v)
            target.set(0f, 0f).addScaledVector(uv1, v.x).addScaledVector(uv2, v.y).addScaledVector(uv3, v.z)
        }

        fun isFrontFacing(a: Vector3, b: Vector3, c: Vector3, direction: Vector3) = Vector3.pool { v0, v1 ->
            // strictly front facing
            v0.subVectors(c, b).cross(v1.subVectors(a, b)).dot(direction) < 0f
        }
    }

    inline fun set(a: Vector3 = this.a, b: Vector3 = this.b, c: Vector3 = this.c): Triangle {
        this.a.copy(a)
        this.b.copy(b)
        this.c.copy(c)
        return this
    }

    inline fun setFromPointsAndIndices(points: Array<Vector3>, i0: Int = 0, i1: Int = i0+1, i2: Int = i1+1): Triangle {
        this.a.copy(points[i0])
        this.b.copy(points[i1])
        this.c.copy(points[i2])
        return this
    }
    inline fun setFromPointsAndIndices(points: List<Vector3>, i0: Int = 0, i1: Int = i0+1, i2: Int = i1+1): Triangle {
        this.a.copy(points[i0])
        this.b.copy(points[i1])
        this.c.copy(points[i2])
        return this
    }

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Vector3) {
        when (key) {
            "a" -> this.a.copy(value)
            "b" -> this.b.copy(value)
            "c" -> this.c.copy(value)
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    operator fun get(key: String): Vector3 =
        when (key) {
            "a" -> this.a
            "b" -> this.b
            "c" -> this.c
            else -> throw IllegalArgumentException("no key $key")
        }
    operator fun set(key: Char, value: Vector3) {
        when (key) {
            'a' -> this.a.copy(value)
            'b' -> this.b.copy(value)
            'c' -> this.c.copy(value)
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    operator fun get(key: Char): Vector3 =
        when (key) {
            'a' -> this.a
            'b' -> this.b
            'c' -> this.c
            else -> throw IllegalArgumentException("no key $key")
        }

    fun clone() = Triangle().copy(this)
    @Suppress("OVERRIDE_BY_INLINE")
    inline fun copy(other: Triangle) = this.set(other.a, other.b, other.c)

    fun getArea() = Vector3.pool { v0, v1 ->
        v0.subVectors(this.c, this.b).cross(v1.subVectors(this.a, this.b)).length() * 0.5f
    }
    inline fun getMidpoint(target: Vector3 = Vector3()) =
        target.addVectors(this.a, this.b).add(this.c).multiplyScalar(1f / 3f)
    inline fun getNormal(target: Vector3 = Vector3()) =
        getNormal(this.a, this.b, this.c, target)
    inline fun getPlane(target: Plane = Plane()) =
        target.setFromCoplanarPoints(this.a, this.b, this.c)
    inline fun getBarycoord(point: Vector3, target: Vector3 = Vector3()) =
        getBarycoord(point, this.a, this.b, this.c, target)
    inline fun getUV(point: Vector3, uv1: Vector2, uv2: Vector2, uv3: Vector2, target: Vector2 = Vector2()) =
        getUV(point, this.a, this.b, this.c, uv1, uv2, uv3, target)
    inline fun containsPoint(point: Vector3) =
        containsPoint(point, this.a, this.b, this.c)
    inline fun isFrontFacing(direction: Vector3) =
        isFrontFacing(this.a, this.b, this.c, direction)
    inline fun intersectsBox(box: Box3) = box.intersectsTriangle(this)

    fun closestPointToPoint(p: Vector3, target: Vector3 = Vector3()) = Vector3.pool { _vab, _vac, _vap ->
        val a = this.a; val b = this.b; val c = this.c

        // algorithm thanks to Real-Time Collision Detection by Christer Ericson,
        // published by Morgan Kaufmann Publishers, (c) 2005 Elsevier Inc.,
        // under the accompanying license; see chapter 5.1.5 for detailed explanation.
        // basically, we're distinguishing which of the voronoi regions of the triangle
        // the point lies in with the minimum amount of redundant computation.

        _vab.subVectors(b, a)
        _vac.subVectors(c, a)
        _vap.subVectors(p, a) // value not used after this chunk
        val d1 = _vab.dot(_vap)
        val d2 = _vac.dot(_vap)
        if (d1 <= 0f && d2 <= 0f) {
            // vertex region of A; barycentric coords (1, 0, 0)
            return target.copy(a)
        }

        val _vbp = _vap.subVectors(p, b)
        val d3 = _vab.dot(_vbp)
        val d4 = _vac.dot(_vbp)
        if (d3 >= 0f && d4 <= d3) {
            // vertex region of B; barycentric coords (0, 1, 0)
            return target.copy(b)
        }

        val vc = d1 * d4 - d3 * d2
        if (vc <= 0f && d1 >= 0 && d3 <= 0f) {
            // edge region of AB; barycentric coords (1-v, v, 0)
            return target.copy(a).addScaledVector(_vab, d1 / (d1 - d3))
        }

        val _vcp = _vap.subVectors(p, c)
        val d5 = _vab.dot(_vcp)
        val d6 = _vac.dot(_vcp)
        if (d6 >= 0f && d5 <= d6) {
            // vertex region of C; barycentric coords (0, 0, 1)
            return target.copy(c)
        }

        val vb = d5 * d2 - d1 * d6
        if (vb <= 0f && d2 >= 0f && d6 <= 0f) {
            // edge region of AC; barycentric coords (1-w, 0, w)
            return target.copy(a).addScaledVector(_vac, d2 / (d2 - d6))
        }

        val va = d3 * d6 - d5 * d4
        if (va <= 0f && (d4 - d3) >= 0f && (d5 - d6) >= 0f) {
            // edge region of BC; barycentric coords (0, 1-w, w)
            return target.copy(b).addScaledVector(_vap.subVectors(c, b), (d4 - d3) / ((d4 - d3) + (d5 - d6))) // edge region of BC
        }

        // face region
        val denom = 1f / (va + vb + vc)
        // u = va * denom
        target.copy(a).addScaledVector(_vab, vb * denom).addScaledVector(_vac, vc * denom)
    }
}
