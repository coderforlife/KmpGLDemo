@file:Suppress("unused", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.*
import kotlin.random.Random


const val DEG2RAD = PI.toFloat() / 180f
const val RAD2DEG = 180f / PI.toFloat()
const val PI_2 = PI.toFloat() * 2f
const val PI_HALF = PI.toFloat() / 2f
const val EPSILON = 1.19e-07f

// Can do this with "native" functions:
//expect fun generateUUID(): String
//Android: actual fun generateUUID(): String = UUID.randomUUID().toString()
//iOS: actual fun generateUUID(): String = NSUUID().UUIDString().lowercase()

private val _lut = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af", "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf", "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf", "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df", "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef", "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff")
@OptIn(ExperimentalUnsignedTypes::class)
fun generateUUID(): String {
    val signedBytes = ByteArray(16)
    Random.nextBytes(signedBytes)
    val bytes = signedBytes.asUByteArray()
    bytes[6] = (bytes[6] and 0x0fu) or 0x40u // set to version 4
    bytes[8] = (bytes[8] and 0x3fu) or 0x80u // set to IETF variant
    return StringBuilder(36)
        .append(_lut[bytes[0].toInt()]).append(_lut[bytes[1].toInt()]).append(_lut[bytes[2].toInt()]).append(_lut[bytes[3].toInt()]).append('-')
        .append(_lut[bytes[4].toInt()]).append(_lut[bytes[5].toInt()]).append('-').append(_lut[bytes[6].toInt()]).append(_lut[bytes[7].toInt()]).append('-')
        .append(_lut[bytes[8].toInt()]).append(_lut[bytes[9].toInt()]).append('-').append(_lut[bytes[10].toInt()]).append(_lut[bytes[11].toInt()])
        .append(_lut[bytes[12].toInt()]).append(_lut[bytes[13].toInt()]).append(_lut[bytes[14].toInt()]).append(_lut[bytes[15].toInt()]).toString()
}

inline fun max(a: Float, b: Float, c: Float) = max(max(a, b), c)
inline fun max(a: Int, b: Int, c: Int) = max(max(a, b), c)

inline fun min(a: Float, b: Float, c: Float) = min(min(a, b), c)
inline fun min(a: Int, b: Int, c: Int) = min(min(a, b), c)

inline fun clamp(value: Float, min: Float, max: Float) = value.coerceIn(min, max)
inline fun clamp(value: Int, min: Int, max: Int) = value.coerceIn(min, max)
inline fun clamp(value: UInt, min: UInt, max: UInt) = value.coerceIn(min, max)

inline fun absDiff(a: UInt, b: UInt) = if (a > b) a - b else b - a

// compute euclidean modulo of m % n
// https://en.wikipedia.org/wiki/Modulo_operation
inline fun euclideanModulo(n: Float, m: Float) = n.mod(m)
inline fun euclideanModulo(n: Int, m: Int) = n.mod(m)

// Linear mapping from range <a1, a2> to range <b1, b2>
inline fun mapLinear(x: Float, a1: Float, a2: Float, b1: Float, b2: Float) = b1+(x-a1)*(b2-b1)/(a2-a1)

// https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/inverse-lerp-a-super-useful-yet-often-overlooked-function-r5230/
inline fun inverseLerp(x: Float, y: Float, value: Float): Float = if (x != y) (value-x)/(y-x) else 0f

// https://en.wikipedia.org/wiki/Linear_interpolation
inline fun lerp(x: Float, y: Float, t: Float) = (1f-t) * x + t * y

// http://www.rorydriscoll.com/2016/03/07/frame-rate-independent-damping-using-lerp/
inline fun damp(x: Float, y: Float, lambda: Float, dt: Float) = lerp(x, y, 1f-exp(-lambda*dt))

// https://www.desmos.com/calculator/vcsjnyz7x4
inline fun pingpong(x: Float, length: Float = 1f) = length - abs(euclideanModulo(x, length*2) -length)

// http://en.wikipedia.org/wiki/Smoothstep
fun smoothstep(x: Float, min: Float, max: Float): Float {
    if (x <= min) return 0f
    if (x >= max) return 1f
    val xs = (x - min) / (max - min)
    return xs * xs * (3f - 2f * xs)
}
fun smootherstep(x: Float, min: Float, max: Float): Float {
    if (x <= min) return 0f
    if (x >= max) return 1f
    val xs = (x - min) / (max - min)
    return xs * xs * xs * (xs * (xs * 6f - 15f) + 10f)
}

// Random integer from <low, high> interval
inline fun randInt(low: Int, high: Int) = Random.nextInt(low, high + 1)

// Random float from <low, high> interval
inline fun randFloat(low: Float, high: Float) = low + Random.nextFloat() * (high - low)

// Random float from <-range/2, range/2> interval
inline fun randFloatSpread(range: Float) = range * (0.5f - Random.nextFloat())

// NOTE: seededRandom() not provided, use built-in Kotlin random methods instead

inline fun degToRad(degrees: Float) = degrees * DEG2RAD
inline fun radToDeg(radians: Float) = radians * RAD2DEG

inline fun isPowerOfTwo(value: Float) = (value%1f) == 0f && isPowerOfTwo(value.toInt())
inline fun ceilPowerOfTwo(value: Float) = 2f.pow(ceil(log2(value)))
inline fun floorPowerOfTwo(value: Float) = 2f.pow(floor(log2(value)))

inline fun isPowerOfTwo(value: Int) = (value and (value-1)) == 0 && value != 0
inline fun ceilPowerOfTwo(value: Int) = 1 shl (Int.SIZE_BITS - (value-1).countLeadingZeroBits())
inline fun floorPowerOfTwo(value: Int) = if (value <= 0) { 0 } else { 1 shl (Int.SIZE_BITS - 1 - value.countLeadingZeroBits()) }

enum class ProperEulerOrder { XYX, YZY, ZXZ, XZX, YXY, ZYZ }

fun setQuaternionFromProperEuler(q: Quaternion, a: Float, b: Float, c: Float, order: ProperEulerOrder) {
    // Intrinsic Proper Euler Angles - see https://en.wikipedia.org/wiki/Euler_angles
    // rotations are applied to the axes in the order specified by 'order'
    // rotation by angle 'a' is applied first, then by angle 'b', then by angle 'c'
    // angles are in radians
    val c2 = cos(b / 2f)
    val s2 = sin(b / 2f)
    val c13 = cos((a + c) / 2f)
    val s13 = sin((a + c) / 2f)
    val c1_3 = cos((a - c) / 2f)
    val s1_3 = sin((a - c) / 2f)
    val c3_1 = cos((c - a) / 2f)
    val s3_1 = sin((c - a) / 2f)
    when (order) {
        ProperEulerOrder.XYX -> q.set(c2 * s13, s2 * c1_3, s2 * s1_3, c2 * c13)
        ProperEulerOrder.YZY -> q.set(s2 * s1_3, c2 * s13, s2 * c1_3, c2 * c13)
        ProperEulerOrder.ZXZ -> q.set(s2 * c1_3, s2 * s1_3, c2 * s13, c2 * c13)
        ProperEulerOrder.XZX -> q.set(c2 * s13, s2 * s3_1, s2 * c3_1, c2 * c13)
        ProperEulerOrder.YXY -> q.set(s2 * c3_1, c2 * s13, s2 * s3_1, c2 * c13)
        ProperEulerOrder.ZYZ -> q.set(s2 * s3_1, s2 * c3_1, c2 * s13, c2 * c13)
    }
}

inline fun <reified T> denormalize(value: T): Float =
    when (value) {
        is Float -> value
        is UShort -> value.toFloat() / 65535f
        is UByte -> value.toFloat() / 255f
        is Short -> max(value / 32767f, -1f)
        is Byte -> max(value / 127f, -1f)
        else -> throw IllegalArgumentException("Invalid component type.")
    }

inline fun <reified T> normalize(value: Float): T =
    when (T::class) {
        Float::class -> value as T
        UShort::class -> (value * 65535f).roundToInt().toUShort() as T
        UByte::class -> (value * 255f).roundToInt().toUByte() as T
        Short::class -> (value * 32767f).roundToInt().toShort() as T
        Byte::class -> (value * 127f).roundToInt().toByte() as T
        else -> throw IllegalArgumentException("Invalid component type.")
    }
