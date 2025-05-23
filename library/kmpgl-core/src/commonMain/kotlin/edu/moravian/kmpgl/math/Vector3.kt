@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ObjectPropertyName",
    "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.*
import kotlin.random.Random

data class Vector3(
    @JvmField var x: Float = 0f,
    @JvmField var y: Float = 0f,
    @JvmField var z: Float = 0f
): Vector<Vector3> {
    override val size: Int get() = 3

    inline fun set(x: Float = this.x,
                   y: Float = this.y,
                   z: Float = this.z): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override inline fun setScalar(scalar: Float): Vector3 {
        this.x = scalar
        this.y = scalar
        this.z = scalar
        return this
    }

    inline fun setX(x: Float): Vector3 {
        this.x = x
        return this
    }
    inline fun setY(y: Float): Vector3 {
        this.y = y
        return this
    }
    inline fun setZ(z: Float): Vector3 {
        this.z = z
        return this
    }

    override inline fun setComponent(index: Int, value: Float): Vector3 {
        when (index) {
            0 -> this.x = value
            1 -> this.y = value
            2 -> this.z = value
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }
        return this
    }

    override inline fun getComponent(index: Int) =
        when (index) {
            0 -> this.x
            1 -> this.y
            2 -> this.z
            else -> throw IndexOutOfBoundsException("index is out of range: $index")
        }

    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: Float) {
        when (key) {
            "x" -> this.x = value
            "y" -> this.y = value
            "z" -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Float =
        when (key) {
            "x" -> this.x
            "y" -> this.y
            "z" -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Float) {
        when (key) {
            'x' -> this.x = value
            'y' -> this.y = value
            'z' -> this.z = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Float =
        when (key) {
            'x' -> this.x
            'y' -> this.y
            'z' -> this.z
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = Vector3(this.x, this.y, this.z)
    override inline fun copy(other: Vector3) = set(other.x, other.y, other.z)

    override inline fun add(v: Vector3): Vector3 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        return this
    }
    override inline fun addScalar(s: Float): Vector3 {
        this.x += s
        this.y += s
        this.z += s
        return this
    }
    override inline fun addVectors(a: Vector3, b: Vector3): Vector3 {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        return this
    }
    override inline fun addScaledVector(v: Vector3, s: Float): Vector3 {
        this.x += v.x * s
        this.y += v.y * s
        this.z += v.z * s
        return this
    }

    override inline fun sub(v: Vector3): Vector3 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        return this
    }
    override inline fun subScalar(s: Float): Vector3 {
        this.x -= s
        this.y -= s
        this.z -= s
        return this
    }
    override inline fun subVectors(a: Vector3, b: Vector3): Vector3 {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        return this
    }

    override inline fun multiply(v: Vector3): Vector3 {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        return this
    }
    override inline fun multiplyScalar(scalar: Float): Vector3 {
        this.x *= scalar
        this.y *= scalar
        this.z *= scalar
        return this
    }
    override inline fun multiplyVectors(a: Vector3, b: Vector3): Vector3 {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        return this
    }

    fun applyEuler(euler: Euler) = Quaternion.pool { q -> this.applyQuaternion(q.setFromEuler(euler)) }

    fun applyAxisAngle(axis: Vector3, angle: Float) = Quaternion.pool { q ->
        this.applyQuaternion(q.setFromAxisAngle(axis, angle))
    }

    fun applyMatrix3(m: Matrix3): Vector3 {
        val x = this.x; val y = this.y; val z = this.z
        val e = m.elements
        this.x = e[0] * x + e[3] * y + e[6] * z
        this.y = e[1] * x + e[4] * y + e[7] * z
        this.z = e[2] * x + e[5] * y + e[8] * z
        return this
    }

    inline fun applyNormalMatrix(m: Matrix3) = this.applyMatrix3(m).normalize()

    fun applyMatrix4(m : Matrix4): Vector3 {
        val x = this.x; val y = this.y; val z = this.z
        val e = m.elements
        val w = 1 / (e[3] * x + e[7] * y + e[11] * z + e[15])
        this.x = (e[0] * x + e[4] * y + e[8] * z + e[12]) * w
        this.y = (e[1] * x + e[5] * y + e[9] * z + e[13]) * w
        this.z = (e[2] * x + e[6] * y + e[10] * z + e[14]) * w
        return this
    }

    fun applyQuaternion(q: Quaternion): Vector3 {
        val x = this.x; val y = this.y; val z = this.z
        val qx = q.x; val qy = q.y; val qz = q.z; val qw = q.w

        // calculate quat * vector
        val ix = qw * x + qy * z - qz * y
        val iy = qw * y + qz * x - qx * z
        val iz = qw * z + qx * y - qy * x
        val iw = - qx * x - qy * y - qz * z

        // calculate result * inverse quat
        this.x = ix * qw + iw * - qx + iy * - qz - iz * - qy
        this.y = iy * qw + iw * - qy + iz * - qx - ix * - qz
        this.z = iz * qw + iw * - qz + ix * - qy - iy * - qx
        return this
    }

    fun transformDirection(m: Matrix4): Vector3 {
        // input: THREE.Matrix4 affine matrix
        // vector interpreted as a direction
        val x = this.x; val y = this.y; val z = this.z
        val e = m.elements
        this.x = e[0] * x + e[4] * y + e[8] * z
        this.y = e[1] * x + e[5] * y + e[9] * z
        this.z = e[2] * x + e[6] * y + e[10] * z
        return this.normalize()
    }

    override inline fun divide(v: Vector3): Vector3 {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        return this
    }

    override inline fun min(v: Vector3): Vector3 {
        this.x = min(this.x, v.x)
        this.y = min(this.y, v.y)
        this.z = min(this.z, v.z)
        return this
    }

    override inline fun max(v: Vector3): Vector3 {
        this.x = max(this.x, v.x)
        this.y = max(this.y, v.y)
        this.z = max(this.z, v.z)
        return this
    }

    override inline fun clamp(min: Vector3, max: Vector3): Vector3 {
        // assumes min < max, componentwise
        this.x = clamp(this.x, min.x, max.x)
        this.y = clamp(this.y, min.y, max.y)
        this.z = clamp(this.z, min.z, max.z)
        return this
    }
    override inline fun clampScalar(minVal: Float, maxVal: Float): Vector3 {
        this.x = clamp(this.x, minVal, maxVal)
        this.y = clamp(this.y, minVal, maxVal)
        this.z = clamp(this.z, minVal, maxVal)
        return this
    }

    override inline fun floor(): Vector3 {
        this.x = floor(this.x)
        this.y = floor(this.y)
        this.z = floor(this.z)
        return this
    }
    inline fun floorToInt(v: IntVector3 = IntVector3()): IntVector3 {
        v.x = this.x.toInt()
        v.y = this.y.toInt()
        v.z = this.z.toInt()
        return v
    }

    override inline fun ceil(): Vector3 {
        this.x = ceil(this.x)
        this.y = ceil(this.y)
        this.z = ceil(this.z)
        return this
    }
    inline fun ceilToInt(v: IntVector3 = IntVector3()): IntVector3 {
        v.x = ceil(this.x).toInt()
        v.y = ceil(this.y).toInt()
        v.z = ceil(this.z).toInt()
        return v
    }

    override inline fun round(): Vector3 {
        this.x = round(this.x)
        this.y = round(this.y)
        this.z = round(this.z)
        return this
    }
    inline fun roundToInt(v: IntVector3 = IntVector3()): IntVector3 {
        v.x = this.x.roundToInt()
        v.y = this.y.roundToInt()
        v.z = this.z.roundToInt()
        return v
    }

    override inline fun roundToZero(): Vector3 {
        this.x = if (this.x < 0f) ceil(this.x) else floor(this.x)
        this.y = if (this.y < 0f) ceil(this.y) else floor(this.y)
        this.z = if (this.z < 0f) ceil(this.z) else floor(this.z)
        return this
    }
    inline fun roundToZeroToInt(v: IntVector3 = IntVector3()): IntVector3 {
        v.x = (if (this.x < 0f) ceil(this.x) else floor(this.x)).toInt()
        v.y = (if (this.y < 0f) ceil(this.y) else floor(this.y)).toInt()
        v.z = (if (this.z < 0f) ceil(this.z) else floor(this.z)).toInt()
        return v
    }

    override inline fun negate(): Vector3 {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
        return this
    }

    override inline infix fun dot(v: Vector3) = this.x * v.x + this.y * v.y + this.z * v.z
    override inline fun lengthSq() = this.x * this.x + this.y * this.y + this.z * this.z
    override inline fun manhattanLength() = abs(this.x) + abs(this.y) + abs(this.z)

    override inline fun lerp(v: Vector3, alpha: Float): Vector3 {
        this.x += (v.x - this.x) * alpha
        this.y += (v.y - this.y) * alpha
        this.z += (v.z - this.z) * alpha
        return this
    }
    override inline fun lerpVectors(v1: Vector3, v2: Vector3, alpha: Float): Vector3 {
        this.x = v1.x + (v2.x - v1.x) * alpha
        this.y = v1.y + (v2.y - v1.y) * alpha
        this.z = v1.z + (v2.z - v1.z) * alpha
        return this
    }

    inline infix fun cross(v: Vector3): Vector3 = this.crossVectors(this, v)
    inline fun crossVectors(a: Vector3, b: Vector3): Vector3 {
        val ax = a.x; val ay = a.y; val az = a.z
        val bx = b.x; val by = b.y; val bz = b.z
        this.x = ay * bz - az * by
        this.y = az * bx - ax * bz
        this.z = ax * by - ay * bx
        return this
    }
    inline operator fun rem(v: Vector3): Vector3 = this.clone().cross(v)
    inline operator fun remAssign(v: Vector3) { this.cross(v) }

    fun projectOnVector(v: Vector3): Vector3 {
        val denominator = v.lengthSq()
        if (denominator == 0f) return this.set(0f, 0f, 0f)
        val scalar = v.dot(this) / denominator
        return this.copy(v).multiplyScalar(scalar)
    }

    fun projectOnPlane(planeNormal: Vector3): Vector3 = pool { v ->
        this.sub(v.copy(this).projectOnVector(planeNormal))
    }

    fun reflect(normal: Vector3): Vector3 = pool { v ->
        // reflect incident vector off plane orthogonal to normal
        // normal is assumed to have unit length
        this.sub(v.copy(normal).multiplyScalar(2f * this.dot(normal)))
    }

    fun angleTo(v: Vector3): Float {
        val denominator = sqrt(this.lengthSq() * v.lengthSq())
        if (denominator == 0f) return PI_HALF
        val theta = this.dot(v) / denominator
        // clamp, to handle numerical problems
        return acos(clamp(theta, -1f, 1f))
    }

    override inline fun distanceToSquared(v: Vector3): Float {
        val dx = this.x - v.x; val dy = this.y - v.y; val dz = this.z - v.z
        return dx * dx + dy * dy + dz * dz
    }
    override inline fun manhattanDistanceTo(v: Vector3): Float =
        abs(this.x - v.x) + abs(this.y - v.y) + abs(this.z - v.z)

    inline fun setFromSpherical(s: Spherical) = this.setFromSphericalCoords(s.radius, s.phi, s.theta)
    inline fun setFromSphericalCoords(radius: Float, phi: Float, theta: Float): Vector3 {
        val sinPhiRadius = sin(phi) * radius
        this.x = sinPhiRadius * sin(theta)
        this.y = cos(phi) * radius
        this.z = sinPhiRadius * cos(theta)
        return this
    }
    inline fun setFromCylindrical(c: Cylindrical) = this.setFromCylindricalCoords(c.radius, c.theta, c.y)
    inline fun setFromCylindricalCoords(radius: Float, theta: Float, y: Float): Vector3 {
        this.x = radius * sin(theta)
        this.y = y
        this.z = radius * cos(theta)
        return this
    }

    inline fun setFromMatrixPosition(m: Matrix4): Vector3 {
        val e = m.elements
        this.x = e[12]
        this.y = e[13]
        this.z = e[14]
        return this
    }
    inline fun setFromMatrixScale(m: Matrix4): Vector3 {
        this.x = this.setFromMatrixColumn(m, 0).length()
        this.y = this.setFromMatrixColumn(m, 1).length()
        this.z = this.setFromMatrixColumn(m, 2).length()
        return this
    }
    inline fun setFromMatrixColumn(m: Matrix4, index: Int): Vector3 = this.fromArray(m.elements, index * 4)
    inline fun setFromMatrix3Column(m: Matrix3, index: Int): Vector3 = this.fromArray(m.elements, index * 3)

    inline fun setFromEuler(e: Euler): Vector3 {
        this.x = e._x
        this.y = e._y
        this.z = e._z
        return this
    }

    inline fun toIntVector() = IntVector3(this.x.toInt(), this.y.toInt(), this.z.toInt())
    inline fun toUIntVector() = UIntVector3(this.x.toUInt(), this.y.toUInt(), this.z.toUInt())
    inline fun toBoolVector() = BoolVector3(this.x != 0f, this.y != 0f, this.z != 0f)

    override fun fromArray(array: FloatArray, offset: Int): Vector3 {
        this.x = array[offset + 0]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        return this
    }
    override inline fun toArray(): FloatArray = floatArrayOf(this.x, this.y, this.z)
    override fun toArray(array: FloatArray, offset: Int): FloatArray {
        array[offset + 0] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        return array
    }

    override fun random(): Vector3 {
        this.x = Random.nextFloat()
        this.y = Random.nextFloat()
        this.z = Random.nextFloat()
        return this
    }

    fun randomDirection(): Vector3 {
        // Derived from https://mathworld.wolfram.com/SpherePointPicking.html
        val u = (Random.nextFloat() - 0.5f) * 2f
        val t = Random.nextFloat() * PI_2
        val f = sqrt(1f - u*u)
        this.x = f * cos(t)
        this.y = f * sin(t)
        this.z = u
        return this
    }

    companion object {
        val pool = Pool { Vector3() }
        val ZERO = Vector3(0f, 0f, 0f)
        val ORIGIN = Vector3(0f, 0f, 0f)
        val ONE = Vector3(1f, 1f, 1f)
        val X_AXIS = Vector3(1f, 0f, 0f)
        val Y_AXIS = Vector3(0f, 1f, 0f)
        val Z_AXIS = Vector3(0f, 0f, 1f)
    }
}
