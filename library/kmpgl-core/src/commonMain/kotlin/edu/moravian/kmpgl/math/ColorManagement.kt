@file:Suppress("unused", "MemberVisibilityCanBePrivate",
    "FunctionName", "ObjectPropertyName", "NOTHING_TO_INLINE")

package edu.moravian.kmpgl.math

import co.touchlab.stately.isolate.IsolateState
import kotlin.jvm.JvmField
import kotlin.math.pow

enum class ColorSpace(val css: String) {
    No(""),
    SRGB("srgb"),
    LinearSRGB( "srgb-linear"),
}

fun SRGBToLinear(c: Float) =
    if (c < 0.04045f) { c*0.0773993808f } else { (c*0.9478672986f+0.0521327014f).pow(2.4f) }
fun LinearToSRGB(c: Float) =
    if (c < 0.0031308f) { c*12.92f } else { 1.055f*c.pow(0.41666f)-0.055f }

object ColorManagement {
    private val legacyModeState = IsolateState { booleanArrayOf(true) }
    var legacyMode: Boolean
        get() = legacyModeState.access { it[0] }
        set(value) { legacyModeState.access { it[0] = value } }
    @JvmField val workingColorSpace = ColorSpace.LinearSRGB

    // RGB-to-RGB transforms, defined as
    // FN[InputColorSpace][OutputColorSpace] callback functions.
    private val FN = mapOf(
        ColorSpace.SRGB to mapOf(ColorSpace.LinearSRGB to ::SRGBToLinear),
        ColorSpace.LinearSRGB to mapOf(ColorSpace.SRGB to ::LinearToSRGB),
    )

    fun <C: Color<C>> convert(color: C, sourceColorSpace: ColorSpace, targetColorSpace: ColorSpace): C {
        if (this.legacyMode || sourceColorSpace === targetColorSpace ||
            sourceColorSpace === ColorSpace.No || targetColorSpace === ColorSpace.No) {
            return color
        }
        val fn = FN[sourceColorSpace]?.get(targetColorSpace)
        if (fn === null) throw RuntimeException("Unsupported color space conversion.")
        val (r, g, b) = color
        color.set(fn(r), fn(g), fn(b))
        return color
    }

    inline fun <C: Color<C>> fromWorkingColorSpace(color: C, targetColorSpace: ColorSpace) =
        this.convert(color, this.workingColorSpace, targetColorSpace)
    inline fun <C: Color<C>> toWorkingColorSpace(color: C, sourceColorSpace: ColorSpace) =
        this.convert(color, sourceColorSpace, this.workingColorSpace)
}
