@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "ObjectPropertyName", "LocalVariableName", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE"
)

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.math.*

data class Color3(
    @JvmField var r: Float = 1f,
    @JvmField var g: Float = 1f,
    @JvmField var b: Float = 1f,
): Color<Color3> {
    override val size: Int get() = 3

    constructor(value: Color3) : this(value.r, value.g, value.b)
    constructor(value: Color4) : this(value.r, value.g, value.b)
    constructor(value: Int) : this() { this.setHex(value) }
    constructor(value: Colors) : this() { this.setHex(value.hex) }
    constructor(value: Float) : this() { this.setScalar(value) }
    constructor(value: String) : this() { this.setStyle(value) }
    constructor(value: HSL) : this() { this.setHSL(value.h, value.s, value.l) }
    constructor(value: FloatArray) : this(value[0], value[1], value[2])

    override inline fun set(value: Color3) = this.copy(value)
    override inline fun set(value: Color4) = this.set(value.r, value.g, value.b)
    override inline fun set(value: Int) = this.setHex(value)
    override inline fun set(value: UInt) = this.setHex(value)
    override inline fun set(value: Colors) = this.setHex(value.hex)
    override inline fun set(value: Float) = this.setScalar(value)
    override inline fun set(value: String) = this.setStyle(value)
    override inline fun set(value: HSL) = this.setHSL(value.h, value.s, value.l)
    override inline fun set(value: FloatArray) = this.set(value[0], value[1], value[2])

    override inline fun set(r: Float, g: Float, b: Float): Color3 {
        this.r = r; this.g = g; this.b = b
        return this
    }
    override inline fun setScalar(scalar: Float): Color3 {
        this.r = scalar; this.g = scalar; this.b = scalar
        return this
    }
    override inline fun setHex(hex: UInt, colorSpace: ColorSpace) =
        this.setRGB((hex shr 16).toInt() and 0xFF, (hex shr 8).toInt() and 0xFF, hex.toInt() and 0xFF, colorSpace)
    override inline fun setRGB(r: Float, g: Float, b: Float, colorSpace: ColorSpace): Color3 {
        this.r = r; this.g = g; this.b = b
        return ColorManagement.toWorkingColorSpace(this, colorSpace)
    }

    override inline fun setHSL(h: Float, s: Float, l: Float, colorSpace: ColorSpace) =
        Color.hsl2rgb(h, s, l, this).also { ColorManagement.toWorkingColorSpace(this, colorSpace) }

    override fun setStyle(style: String, colorSpace: ColorSpace): Color3 {
        val alpha = Color.parseStyle(style, this, colorSpace)
        //if (alpha < 1f) warning...
        return this
    }

    override inline fun setColorName(style: String, colorSpace: ColorSpace) = setHex(Colors.getHex(style), colorSpace)

    // same as set
    override inline fun update(r: Float, g: Float, b: Float) = set(r, g, b)
    override inline fun updateRGB(r: Float, g: Float, b: Float, colorSpace: ColorSpace) = setRGB(r, g, b, colorSpace)
    override inline fun updateHSL(h: Float, s: Float, l: Float, colorSpace: ColorSpace) = setHSL(h, s, l, colorSpace)


    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: Float) {
        when (key) {
            "r" -> this.r = value
            "g" -> this.g = value
            "b" -> this.b = value
            "a" -> {}
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Float =
        when (key) {
            "r" -> this.r
            "g" -> this.g
            "b" -> this.b
            "a" -> 1f
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Float) {
        when (key) {
            'r' -> this.r = value
            'g' -> this.g = value
            'b' -> this.b = value
            'a' -> {}
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Float =
        when (key) {
            'r' -> this.r
            'g' -> this.g
            'b' -> this.b
            'a' -> 1f
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = Color3(this.r, this.g, this.b)
    override inline fun copy(other: Color3) = this.set(other.r, other.g, other.b)

    inline fun copySRGBToLinear(color: Color3): Color3 {
        this.r = SRGBToLinear(color.r)
        this.g = SRGBToLinear(color.g)
        this.b = SRGBToLinear(color.b)
        return this
    }
    inline fun copyLinearToSRGB(color: Color3): Color3 {
        this.r = LinearToSRGB(color.r)
        this.g = LinearToSRGB(color.g)
        this.b = LinearToSRGB(color.b)
        return this
    }
    inline fun convertSRGBToLinear() = this.copySRGBToLinear(this)
    inline fun convertLinearToSRGB() = this.copyLinearToSRGB(this)

    override fun getHex(colorSpace: ColorSpace): UInt {
        val (r, g, b) = pool { rgb -> ColorManagement.fromWorkingColorSpace(rgb.copy(this), colorSpace) }
        return ((clamp((r*255).roundToInt(), 0, 255) shl 16) or
                (clamp((g*255).roundToInt(), 0, 255) shl 8) or
                clamp((b*255).roundToInt(), 0, 255)).toUInt() or 0xFF000000u
    }
    override inline fun getHexString(colorSpace: ColorSpace) =
        this.getHex(colorSpace).toString(16).substring(2)
    override fun getHSL(target: HSL, colorSpace: ColorSpace): HSL {
        val (r, g, b) = pool { rgb -> ColorManagement.fromWorkingColorSpace(rgb.copy(this), colorSpace) }
        return Color.rgb2hsl(r, g, b, target)
    }
    override inline fun getRGB(target: Color3, colorSpace: ColorSpace) =
        ColorManagement.fromWorkingColorSpace(target.copy(this), colorSpace)
    override fun getStyle(colorSpace: ColorSpace): String {
        val (r, g, b) = pool { rgb -> ColorManagement.fromWorkingColorSpace(rgb.copy(this), colorSpace) }
        return if (colorSpace !== ColorSpace.SRGB) {
            // Requires CSS Color Module Level 4 (https://www.w3.org/TR/css-color-4/).
            "color(${colorSpace.css} $r $g $b)"
        } else {
            "rgb(${(r * 255).roundToInt()},${(g * 255).roundToInt()},${(b * 255).roundToInt()})"
        }
    }

    override inline fun add(color: Color3): Color3 {
        this.r += color.r
        this.g += color.g
        this.b += color.b
        return this
    }
    override inline fun addColors(color1: Color3, color2: Color3): Color3 {
        this.r = color1.r + color2.r
        this.g = color1.g + color2.g
        this.b = color1.b + color2.b
        return this
    }
    override inline fun addScalar(s: Float): Color3 {
        this.r += s
        this.g += s
        this.b += s
        return this
    }

    override inline fun sub(color: Color3): Color3 {
        this.r = max(0f, this.r - color.r)
        this.g = max(0f, this.g - color.g)
        this.b = max(0f, this.b - color.b)
        return this
    }
    override inline fun subScalar(s: Float): Color3 {
        this.r = max(0f, this.r - s)
        this.g = max(0f, this.g - s)
        this.b = max(0f, this.b - s)
        return this
    }

    override inline fun multiply(color: Color3): Color3 {
        this.r *= color.r
        this.g *= color.g
        this.b *= color.b
        return this
    }
    override inline fun multiplyScalar(s: Float): Color3 {
        this.r *= s
        this.g *= s
        this.b *= s
        return this
    }

    override inline fun lerp(color: Color3, alpha: Float): Color3 {
        this.r += (color.r - this.r) * alpha
        this.g += (color.g - this.g) * alpha
        this.b += (color.b - this.b) * alpha
        return this
    }
    override inline fun lerpColors(color1: Color3, color2: Color3, alpha: Float): Color3 {
        this.r = color1.r + (color2.r - color1.r) * alpha
        this.g = color1.g + (color2.g - color1.g) * alpha
        this.b = color1.b + (color2.b - color1.b) * alpha
        return this
    }
    override fun lerpHSL(color: Color3, alpha: Float): Color3 =
        HSL.pool { hslA, hslB ->
            val (aH, aS, aL) = this.getHSL(hslA)
            val (bH, bS, bL) = color.getHSL(hslB)
            val h = lerp(aH, bH, alpha)
            val s = lerp(aS, bS, alpha)
            val l = lerp(aL, bL, alpha)
            this.setHSL(h, s, l)
        }

    inline fun toVector(v: Vector3 = Vector3()) = v.set(this.r, this.g, this.b)
    inline fun toIntVector(v: IntVector3 = IntVector3()) = v.set((this.r*255).toInt(), (this.g*255).toInt(), (this.b*255).toInt())
    inline fun toUIntVector(v: UIntVector3 = UIntVector3()) = v.set((this.r*255).toUInt(), (this.g*255).toUInt(), (this.b*255).toUInt())

    override inline fun fromArray(array: FloatArray, offset: Int): Color3 {
        this.r = array[offset + 0]
        this.g = array[offset + 1]
        this.b = array[offset + 2]
        return this
    }
    override inline fun toArray() = floatArrayOf(this.r, this.g, this.b)
    override fun toArray(array: FloatArray, offset: Int): FloatArray {
        array[offset + 0] = this.r
        array[offset + 1] = this.g
        array[offset + 2] = this.b
        return array
    }

    companion object {
        val pool = Pool { Color3() }
    }
}

inline fun Color3(value: UInt) = Color3().setHex(value)
