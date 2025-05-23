@file:Suppress("NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import kotlin.math.min

sealed interface Color<C: Color<C>> {
    val size: Int

    operator fun component1(): Float
    operator fun component2(): Float
    operator fun component3(): Float
    operator fun component4(): Float = 1f

    fun set(value: Color3): C
    fun set(value: Color4): C
    fun set(value: Int): C
    fun set(value: UInt): C
    fun set(value: Colors): C
    fun set(value: Float): C
    fun set(value: String): C
    fun set(value: HSL): C
    fun set(value: FloatArray): C

    fun set(r: Float, g: Float, b: Float): C
    fun setScalar(scalar: Float): C
    fun setHex(hex: UInt, colorSpace: ColorSpace): C
    fun setRGB(r: Float, g: Float, b: Float, colorSpace: ColorSpace): C
    fun setHSL(h: Float, s: Float, l: Float, colorSpace: ColorSpace): C
    fun setStyle(style: String, colorSpace: ColorSpace): C
    fun setColorName(style: String, colorSpace: ColorSpace): C

    // same as set but don't change a for Color4
    fun update(r: Float, g: Float, b: Float): C = set(r, g, b)
    fun updateRGB(r: Float, g: Float, b: Float, colorSpace: ColorSpace): C
    fun updateHSL(h: Float, s: Float, l: Float, colorSpace: ColorSpace): C

    // these are required to support javascript-like property lookup
    operator fun set(key: String, value: Float)
    operator fun get(key: String): Float
    operator fun set(key: Char, value: Float)
    operator fun get(key: Char): Float

    fun clone(): C
    fun copy(other: C): C

    fun getHex(colorSpace: ColorSpace): UInt
    fun getHexString(colorSpace: ColorSpace): String
    fun getHSL(target: HSL, colorSpace: ColorSpace): HSL
    fun getRGB(target: C, colorSpace: ColorSpace): C
    fun getStyle(colorSpace: ColorSpace): String

    fun add(color: C): C
    fun addColors(color1: C, color2: C): C
    fun addScalar(s: Float): C
    fun sub(color: C): C
    fun subScalar(s: Float): C
    fun multiply(color: C): C
    fun multiplyScalar(s: Float): C

    fun lerp(color: C, alpha: Float): C
    fun lerpColors(color1: C, color2: C, alpha: Float): C
    fun lerpHSL(color: C, alpha: Float): C

    fun fromArray(array: FloatArray, offset: Int): C
    fun toArray(): FloatArray
    fun toArray(array: FloatArray, offset: Int): FloatArray

    companion object {
        private fun hue2rgb(p: Float, q: Float, t: Float): Float {
            var T = t
            if (T < 0f) T += 1f
            if (T > 1f) T -= 1f
            if (T < 1f / 6f) return p + (q - p) * 6f * T
            if (T < 1f / 2f) return q
            if (T < 2f / 3f) return p + (q - p) * 6f * (2f / 3f - T)
            return p
        }
        fun <C: Color<C>> hsl2rgb(h: Float, s: Float, l: Float, color: C): C {
            // h,s,l ranges are in 0.0 - 1.0
            val H = euclideanModulo(h, 1f)
            val S = clamp(s, 0f, 1f)
            val L = clamp(l, 0f, 1f)
            return if (S == 0f) {
                color.set(L, L, L)
            } else {
                val p = if (L <= 0.5) {
                    L * (1f + S)
                } else {
                    L + S - (L * S)
                }
                val q = (2f * L) - p
                color.set(
                    hue2rgb(q, p, H + 1f / 3f),
                    hue2rgb(q, p, H),
                    hue2rgb(q, p, H - 1f / 3f)
                )
            }
        }
        fun rgb2hsl(r: Float, g: Float, b: Float, target: HSL): HSL {
            // h,s,l ranges are in 0.0 - 1.0
            val max = max(r, g, b)
            val min = min(r, g, b)
            val lightness = (min + max) * 0.5f
            if (min == max) { return target.set(0f, 0f, lightness) }

            val delta = max - min
            val saturation = if (lightness <= 0.5f) { delta/(max+min) } else { delta/(2f-max-min) }
            val hue = when (max) {
                r -> (g - b) / delta + (if (g < b) 6f else 0f)
                g -> (b - r) / delta + 2f
                b -> (r - g) / delta + 4f
                else -> 0f
            }
            return target.set(hue / 6f, saturation, lightness)
        }

        private const val _reInt = "\\s*(\\d+)\\s*"
        private const val _rePercent = "\\s*(\\d+)%\\s*"
        private const val _reFloat = "\\s*(\\d*\\.?\\d+|\\d+\\.)\\s*"
        private const val _reFloatPercent = "\\s*(\\d*\\.?\\d+|\\d+\\.)%\\s*"
        @Suppress("RegExpRedundantEscape")
        private val reHex = Regex("\\#([A-Fa-f0-9]{3}[A-Fa-f0-9]{3}?)") // TODO: allowed to be 3, 4, 6, or 8 in length
        private val reRGB = Regex("((?:rgb|hsl)a?)\\(([^)]*)\\)")
        private val reRGBInt = Regex("$_reInt,$_reInt,$_reInt(?:,$_reFloat)?")
        private val reRGBPercent = Regex("$_rePercent,$_rePercent,$_rePercent(?:,$_reFloat)?")
        private val reHSL = Regex("$_reFloat,$_reFloatPercent,$_reFloatPercent(?:,$_reFloat)?")

        fun <C: Color<C>> parseStyle(origStyle: String, color: C, colorSpace: ColorSpace): Float {
            val style = origStyle.lowercase().trim()
            if (style.isNotEmpty() && style in Colors.keywords) {
                color.setHex(Colors.keywords[style]!!, colorSpace)
                return 1f
            }

            if (reHex.matches(style)) { // hex color
                val hex = reHex.matchEntire(style)!!.groupValues[1]
                return if (hex.length <= 4) {
                    color.setRGB( // #ff0 or #ff0f
                        hex.substring(0, 1).toInt(16) * 17,
                        hex.substring(1, 2).toInt(16) * 17,
                        hex.substring(2, 3).toInt(16) * 17)
                    if (hex.length == 4) hex.substring(3, 4).toInt(16) * 17 / 255f else 1f
                } else {
                    color.setRGB( // #ff0000 or #ff0000ff
                        hex.substring(0, 2).toInt(16),
                        hex.substring(2, 4).toInt(16),
                        hex.substring(4, 6).toInt(16))
                    if (hex.length == 8) hex.substring(6, 8).toInt(16) * 17 / 255f else 1f
                }
            } else if (reRGB.matches(style)) { // rgb / hsl
                val (_, name, components) = reRGB.matchEntire(style)!!.groupValues
                when (name) {
                    "rgb", "rgba" -> {
                        if (reRGBInt.matches(components)) { // rgb(255,0,0) rgba(255,0,0,0.5)
                            val (r, g, b, alpha) = getColorValues(components, reRGBInt)
                            color.setRGB(
                                min(255, r.toInt()),
                                min(255, g.toInt()),
                                min(255, b.toInt()), colorSpace)
                            return alpha.toFloatOrNull() ?: 1f
                        } else if (reRGBPercent.matches(components)) { // rgb(100%,0%,0%) rgba(100%,0%,0%,0.5)
                            val (r, g, b, alpha) = getColorValues(components, reRGBPercent)
                            color.setRGB(
                                min(100, r.toInt()) / 100f,
                                min(100, g.toInt()) / 100f,
                                min(100, b.toInt()) / 100f, colorSpace)
                            return alpha.toFloatOrNull() ?: 1f
                        }
                    }
                    "hsl", "hsla" -> {
                        if (reHSL.matches(components)) { // hsl(120,50%,50%) hsla(120,50%,50%,0.5)
                            val (h, s, l, alpha) = getColorValues(components, reHSL)
                            color.setHSL(
                                h.toFloat() / 360f,
                                s.toFloat() / 100f,
                                l.toFloat() / 100f, colorSpace)
                            return alpha.toFloatOrNull() ?: 1f
                        }
                    }
                }
            }

            throw IllegalArgumentException("Color: Unknown color style: $origStyle")
        }
        private inline fun getColorValues(components: String, re: Regex): Array<String> {
            val c = re.matchEntire(components)!!.groupValues
            return arrayOf(c[1], c[2], c[3], c.getOrElse(4) { "1" })
        }
    }
}

data class HSL(var h: Float = 0f, var s: Float = 0f, var l: Float = 0f) {
    inline fun set(h: Float = this.h, s: Float = this.s, l: Float = this.s): HSL {
        this.h = h
        this.s = s
        this.l = l
        return this
    }
    companion object {
        internal val pool = Pool { HSL() }
    }
}

inline fun <C: Color<C>> C.setHex(hex: UInt) = setHex(hex, ColorSpace.SRGB)
inline fun <C: Color<C>> C.setHex(hex: Int, colorSpace: ColorSpace = ColorSpace.SRGB) = setHex(hex.toUInt(), colorSpace)
inline fun <C: Color<C>> C.setRGB(r: Float, g: Float, b: Float) = setRGB(r, g, b, ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.setRGB(r: Int, g: Int, b: Int, colorSpace: ColorSpace = ColorManagement.workingColorSpace) = setRGB(r/255f, g/255f, b/255f, colorSpace)
inline fun <C: Color<C>> C.setHSL(h: Float, s: Float, l: Float) = setHSL(h, s, l, ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.setHSL(hsl: HSL, colorSpace: ColorSpace = ColorManagement.workingColorSpace) = setHSL(hsl.h, hsl.s, hsl.l, colorSpace)
inline fun <C: Color<C>> C.setStyle(style: String) = setStyle(style, ColorSpace.SRGB)
inline fun <C: Color<C>> C.setColorName(style: String) = setColorName(style, ColorSpace.SRGB)

inline fun <C: Color<C>> C.updateRGB(r: Float, g: Float, b: Float) = updateRGB(r, g, b, ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.updateRGB(r: Int, g: Int, b: Int, colorSpace: ColorSpace = ColorManagement.workingColorSpace) = updateRGB(r/255f, g/255f, b/255f, colorSpace)
inline fun <C: Color<C>> C.updateHSL(h: Float, s: Float, l: Float) = updateHSL(h, s, l, ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.updateHSL(hsl: HSL, colorSpace: ColorSpace = ColorManagement.workingColorSpace): C = updateHSL(hsl.h, hsl.s, hsl.l, colorSpace)

inline fun <C: Color<C>> C.getHex() = getHex(ColorSpace.SRGB)
inline fun <C: Color<C>> C.getHexString() = getHexString(ColorSpace.SRGB)
inline fun <C: Color<C>> C.getHSL() = getHSL(HSL(), ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.getHSL(target: HSL) = getHSL(target, ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.getHSL(colorSpace: ColorSpace) = getHSL(HSL(), colorSpace)
inline fun <C: Color<C>> C.getRGB() = getRGB(clone(), ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.getRGB(target: C) = getRGB(target, ColorManagement.workingColorSpace)
inline fun <C: Color<C>> C.getRGB(colorSpace: ColorSpace) = getRGB(clone(), colorSpace)
inline fun <C: Color<C>> C.getStyle() = getStyle(ColorSpace.SRGB)

inline fun <C: Color<C>> C.add(s: Float) = this.addScalar(s)
inline operator fun <C: Color<C>> C.plus(color: C) = this.clone().add(color)
inline operator fun <C: Color<C>> C.plus(s: Float) = this.clone().addScalar(s)
inline operator fun <C: Color<C>> C.plusAssign(color: C) { this.add(color) }
inline operator fun <C: Color<C>> C.plusAssign(s: Float) { this.addScalar(s) }
inline fun <C: Color<C>> C.sub(s: Float) = this.subScalar(s)
inline operator fun <C: Color<C>> C.minus(color: C) = this.clone().sub(color)
inline operator fun <C: Color<C>> C.minus(s: Float) = this.clone().subScalar(s)
inline operator fun <C: Color<C>> C.minusAssign(color: C) { this.sub(color) }
inline operator fun <C: Color<C>> C.minusAssign(s: Float) { this.subScalar(s) }
inline fun <C: Color<C>> C.multiply(s: Float) = this.multiplyScalar(s)
inline operator fun <C: Color<C>> C.times(color: C) = this.clone().multiply(color)
inline operator fun <C: Color<C>> C.times(s: Float) = this.clone().multiplyScalar(s)
inline operator fun <C: Color<C>> C.timesAssign(color: C) { this.multiply(color) }
inline operator fun <C: Color<C>> C.timesAssign(s: Float) { this.multiplyScalar(s) }


fun <C: Color<C>> C.offsetHSL(h: Float, s: Float, l: Float): C {
    val (aH, aS, aL) = HSL.pool { hsl -> this.getHSL(hsl) }
    return this.updateHSL(aH + h, aS + s, aL + l)
}
inline fun <C: Color<C>> C.offsetHSL(hsl: HSL) = offsetHSL(hsl.h, hsl.s, hsl.l)

inline fun <C: Color<C>> C.fromArray(array: FloatArray) = fromArray(array, 0)
inline fun <C: Color<C>> C.toArray(array: FloatArray) = toArray(array, 0)

