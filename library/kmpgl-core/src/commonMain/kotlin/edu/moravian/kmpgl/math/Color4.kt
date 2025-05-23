@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "ObjectPropertyName", "LocalVariableName", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE"
)

package edu.moravian.kmpgl.math

import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.roundToInt

data class Color4(
    @JvmField var r: Float = 1f,
    @JvmField var g: Float = 1f,
    @JvmField var b: Float = 1f,
    @JvmField var a: Float = 1f,
): Color<Color4> {
    override val size: Int get() = 3

    constructor(value: Color3, alpha: Float = 1f) : this(value.r, value.g, value.b, alpha)
    constructor(value: Color4) : this(value.r, value.g, value.b, value.a)
    constructor(value: Int) : this() { this.setHex(value) }
    constructor(value: Int, alpha: Float) : this() { this.setHex(value, alpha) }
    constructor(value: Colors, alpha: Float = 1f) : this() { this.setHex(value.hex); this.a = alpha }
    constructor(value: Float, alpha: Float = 1f) : this() { this.setScalar(value, alpha) }
    constructor(value: String) : this() { this.setStyle(value) }
    constructor(value: String, alpha: Float = 1f) : this() { this.setStyle(value, alpha) }
    constructor(value: HSL) : this() { this.setHSL(value.h, value.s, value.l) }
    constructor(value: FloatArray) : this(value[0], value[1], value[2], value[3])

    override inline fun set(value: Color3) = this.set(value.r, value.g, value.b)
    override inline fun set(value: Color4) = this.copy(value)
    override inline fun set(value: Int) = this.setHex(value)
    override inline fun set(value: UInt) = this.setHex(value)
    override inline fun set(value: Colors) = this.setHex(value.hex, 1f)
    override inline fun set(value: Float) = this.setScalar(value, 1f)
    override inline fun set(value: String) = this.setStyle(value)
    override inline fun set(value: HSL) = this.setHSL(value.h, value.s, value.l)
    override inline fun set(value: FloatArray) = this.set(value[0], value[1], value[2], value.getOrElse(3) { 1f })

    inline fun set(value: Color3, alpha: Float) = this.set(value.r, value.g, value.b, alpha)
    inline fun set(value: Int, alpha: Float) = this.setHex(value, alpha)
    inline fun set(value: UInt, alpha: Float) = this.setHex(value, alpha)
    inline fun set(value: Colors, alpha: Float) = this.setHex(value.hex, alpha)
    inline fun set(value: Float, alpha: Float) = this.setScalar(value, alpha)

    override inline fun set(r: Float, g: Float, b: Float): Color4 {
        this.r = r; this.g = g; this.b = b; this.a = 1f
        return this
    }
    inline fun set(r: Float, g: Float, b: Float, a: Float): Color4 {
        this.r = r; this.g = g; this.b = b; this.a = a
        return this
    }
    override inline fun setScalar(scalar: Float): Color4 {
        this.r = scalar; this.g = scalar; this.b = scalar; this.a = 1f
        return this
    }
    inline fun setScalar(scalar: Float, alpha: Float): Color4 {
        this.r = scalar; this.g = scalar; this.b = scalar; this.a = alpha
        return this
    }
    inline fun setHex(hex: Int, alpha: Int, colorSpace: ColorSpace = ColorSpace.SRGB) = setHex(hex, alpha/255f, colorSpace)
    inline fun setHex(hex: Int, alpha: Float, colorSpace: ColorSpace = ColorSpace.SRGB) =
        this.setRGB((hex ushr 16) and 0xFF, (hex ushr 8) and 0xFF, hex and 0xFF, alpha, colorSpace)
    override inline fun setHex(hex: UInt, colorSpace: ColorSpace) =
        setRGB((hex shr 16).toInt() and 0xFF, (hex shr 8).toInt() and 0xFF, hex.toInt() and 0xFF, (hex shr 24).toInt() and 0xFF, colorSpace)
    inline fun setHex(hex: UInt, alpha: Int, colorSpace: ColorSpace = ColorSpace.SRGB) = setHex(hex, alpha/255f, colorSpace)
    inline fun setHex(hex: UInt, alpha: Float, colorSpace: ColorSpace = ColorSpace.SRGB) =
        this.setRGB((hex shr 16).toInt() and 0xFF, (hex shr 8).toInt() and 0xFF, hex.toInt() and 0xFF, alpha, colorSpace)

    override inline fun setRGB(r: Float, g: Float, b: Float, colorSpace: ColorSpace) = this.setRGB(r, g, b, 1f, colorSpace)
    inline fun setRGB(r: Float, g: Float, b: Float, a: Float, colorSpace: ColorSpace = ColorManagement.workingColorSpace): Color4 {
        this.r = r; this.g = g; this.b = b; this.a = a
        return ColorManagement.toWorkingColorSpace(this, colorSpace)
    }
    inline fun setRGB(r: Int, g: Int, b: Int, a: Float = 1f, colorSpace: ColorSpace = ColorManagement.workingColorSpace) =
        this.setRGB(r/255f, g/255f, b/255f, a, colorSpace)
    inline fun setRGB(r: Int, g: Int, b: Int, a: Int, colorSpace: ColorSpace = ColorManagement.workingColorSpace) =
        this.setRGB(r/255f, g/255f, b/255f, a/255f, colorSpace)

    override fun setHSL(h: Float, s: Float, l: Float, colorSpace: ColorSpace) = setHSL(h, s, l, 1f, colorSpace)
    inline fun setHSL(h: Float, s: Float, l: Float, a: Float, colorSpace: ColorSpace = ColorManagement.workingColorSpace) =
        updateHSL(h, s, l, colorSpace).also { this.a = a }
    inline fun setHSL(hsl: HSL, alpha: Float, colorSpace: ColorSpace = ColorManagement.workingColorSpace) =
        setHSL(hsl.h, hsl.s, hsl.l, alpha, colorSpace)

    override inline fun setStyle(style: String, colorSpace: ColorSpace): Color4 {
        this.a = Color.parseStyle(style, this, colorSpace)
        return this
    }
    fun setStyle(style: String, alpha: Float, colorSpace: ColorSpace = ColorSpace.SRGB): Color4 {
        Color.parseStyle(style, this, colorSpace)
        this.a = alpha
        return this
    }

    override inline fun setColorName(style: String, colorSpace: ColorSpace) = setColorName(style, 1f, colorSpace)
    fun setColorName(style: String, alpha: Float, colorSpace: ColorSpace = ColorSpace.SRGB) = setHex(Colors.getHex(style), alpha, colorSpace)

    // same as set but don't change a
    override inline fun update(r: Float, g: Float, b: Float): Color4 {
        this.r = r; this.g = g; this.b = b
        return this
    }
    override inline fun updateRGB(r: Float, g: Float, b: Float, colorSpace: ColorSpace) =
        ColorManagement.toWorkingColorSpace(this.update(r, g, b), colorSpace)
    override inline fun updateHSL(h: Float, s: Float, l: Float, colorSpace: ColorSpace) =
        ColorManagement.toWorkingColorSpace(Color.hsl2rgb(h, s, l, this), colorSpace)


    // these are required to support javascript-like property lookup
    override operator fun set(key: String, value: Float) {
        when (key) {
            "r" -> this.r = value
            "g" -> this.g = value
            "b" -> this.b = value
            "a" -> this.a = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: String): Float =
        when (key) {
            "r" -> this.r
            "g" -> this.g
            "b" -> this.b
            "a" -> this.a
            else -> throw IllegalArgumentException("no key $key")
        }
    override operator fun set(key: Char, value: Float) {
        when (key) {
            'r' -> this.r = value
            'g' -> this.g = value
            'b' -> this.b = value
            'a' -> this.a = value
            else -> throw IllegalArgumentException("no key $key")
        }
    }
    override operator fun get(key: Char): Float =
        when (key) {
            'r' -> this.r
            'g' -> this.g
            'b' -> this.b
            'a' -> this.a
            else -> throw IllegalArgumentException("no key $key")
        }

    override fun clone() = Color4(this.r, this.g, this.b, this.a)
    override inline fun copy(other: Color4) = this.set(other.r, other.g, other.b, other.a)

    override fun getHex(colorSpace: ColorSpace): UInt {
        val (r, g, b) = pool { rgb -> ColorManagement.fromWorkingColorSpace(rgb.copy(this), colorSpace) }
        return ((clamp((a*255).roundToInt(), 0, 255) shl 24) or
                (clamp((r*255).roundToInt(), 0, 255) shl 16) or
                (clamp((g*255).roundToInt(), 0, 255) shl 8) or
                clamp((b*255).roundToInt(), 0, 255)).toUInt()
    }
    override inline fun getHexString(colorSpace: ColorSpace) =
        this.getHex(colorSpace).toString(16).padStart(8, '0')
    override fun getHSL(target: HSL, colorSpace: ColorSpace): HSL {
        val (r, g, b) = pool { rgb -> ColorManagement.fromWorkingColorSpace(rgb.copy(this), colorSpace) }
        return Color.rgb2hsl(r, g, b, target)
    }
    override inline fun getRGB(target: Color4, colorSpace: ColorSpace) =
        ColorManagement.fromWorkingColorSpace(target.copy(this), colorSpace)
    override fun getStyle(colorSpace: ColorSpace): String {
        val (r, g, b) = pool { rgb -> ColorManagement.fromWorkingColorSpace(rgb.copy(this), colorSpace) }
        return if (colorSpace !== ColorSpace.SRGB) {
            // Requires CSS Color Module Level 4 (https://www.w3.org/TR/css-color-4/).
            "color(${colorSpace.css} $r $g $b / $a)"
        } else {
            "rgba(${(r * 255).roundToInt()},${(g * 255).roundToInt()},${(b * 255).roundToInt()},$a)"
        }
    }

    override inline fun add(color: Color4): Color4 {
        this.r += color.r
        this.g += color.g
        this.b += color.b
        this.a += color.a
        return this
    }
    override inline fun addColors(color1: Color4, color2: Color4): Color4 {
        this.r = color1.r + color2.r
        this.g = color1.g + color2.g
        this.b = color1.b + color2.b
        this.a = color1.a + color2.a
        return this
    }
    override inline fun addScalar(s: Float): Color4 {
        this.r += s
        this.g += s
        this.b += s
        this.a += s
        return this
    }

    override inline fun sub(color: Color4): Color4 {
        this.r = max(0f, this.r - color.r)
        this.g = max(0f, this.g - color.g)
        this.b = max(0f, this.b - color.b)
        this.a = max(0f, this.a - color.a)
        return this
    }
    override inline fun subScalar(s: Float): Color4 {
        this.r = max(0f, this.r - s)
        this.g = max(0f, this.g - s)
        this.b = max(0f, this.b - s)
        this.a = max(0f, this.a - s)
        return this
    }

    override inline fun multiply(color: Color4): Color4 {
        this.r *= color.r
        this.g *= color.g
        this.b *= color.b
        this.a *= color.a
        return this
    }
    override inline fun multiplyScalar(s: Float): Color4 {
        this.r *= s
        this.g *= s
        this.b *= s
        this.a *= s
        return this
    }


    inline fun mix(color: Color4): Color4 {
        this.r = max(color.r * color.a + this.r * this.a, 1f)
        this.g = max(color.g * color.a + this.g * this.a, 1f)
        this.b = max(color.b * color.a + this.b * this.a, 1f)
        this.a = max(color.a + this.a, 1f)
        return this
    }

    override inline fun lerp(color: Color4, alpha: Float): Color4 {
        this.r += (color.r - this.r) * alpha
        this.g += (color.g - this.g) * alpha
        this.b += (color.b - this.b) * alpha
        this.a += (color.a - this.a) * alpha
        return this
    }
    override inline fun lerpColors(color1: Color4, color2: Color4, alpha: Float): Color4 {
        this.r = color1.r + (color2.r - color1.r) * alpha
        this.g = color1.g + (color2.g - color1.g) * alpha
        this.b = color1.b + (color2.b - color1.b) * alpha
        this.a = color1.a + (color2.a - color1.a) * alpha
        return this
    }
    override fun lerpHSL(color: Color4, alpha: Float): Color4 =
        HSL.pool { hslA, hslB ->
            val (aH, aS, aL) = this.getHSL(hslA)
            val (bH, bS, bL) = color.getHSL(hslB)
            val h = lerp(aH, bH, alpha)
            val s = lerp(aS, bS, alpha)
            val l = lerp(aL, bL, alpha)
            this.a += (color.a - this.a) * alpha
            this.updateHSL(h, s, l)
        }

    inline fun toVector(v: Vector4 = Vector4()) = v.set(this.r, this.g, this.b, this.a)
    inline fun toIntVector(v: IntVector4 = IntVector4()) = v.set((this.r*255).toInt(), (this.g*255).toInt(), (this.b*255).toInt(), (this.a*255).toInt())
    inline fun toUIntVector(v: UIntVector4 = UIntVector4()) = v.set((this.r*255).toUInt(), (this.g*255).toUInt(), (this.b*255).toUInt(), (this.a*255).toUInt())

    override inline fun fromArray(array: FloatArray, offset: Int): Color4 {
        this.r = array[offset + 0]
        this.g = array[offset + 1]
        this.b = array[offset + 2]
        this.a = array[offset + 3]
        return this
    }
    override inline fun toArray() = floatArrayOf(this.r, this.g, this.b, this.a)
    override fun toArray(array: FloatArray, offset: Int): FloatArray {
        array[offset + 0] = this.r
        array[offset + 1] = this.g
        array[offset + 2] = this.b
        array[offset + 3] = this.a
        return array
    }

    companion object {
        val pool = Pool { Color4() }
        private val hslPool = Pool { HSL() }
    }
}

inline fun Color4(value: UInt) = Color4().setHex(value)
inline fun Color4(value: UInt, alpha: Float) = Color4().setHex(value, alpha)
