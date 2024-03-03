package com.dafay.demo.colorpicker

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Size

/**
 * @Des 颜色转换类
 * @Author lipengfei
 * @Date 2024/1/3
 */
object MColorConvertUtils {
    /**
     * Convert HSV components to an ARGB color. Alpha set to 0xFF.
     *
     *  * `hsv[0]` is Hue \([0..360[\)
     *  * `hsv[1]` is Saturation \([0...1]\)
     *  * `hsv[2]` is Value \([0...1]\)
     *
     * If hsv values are out of range, they are pinned.
     * @param hsv  3 element array which holds the input HSV components.
     * @return the resulting argb color
     */
    @ColorInt
    fun HSVToColor(@Size(3) hsv: FloatArray?): Int {
        return Color.HSVToColor(0xFF, hsv)
    }

    @ColorInt
    fun HSVToColor(
        @FloatRange(from = 0.0, to = 360.0) hue: Float,
        @FloatRange(from = 0.0, to = 1.0) saturation: Float,
        @FloatRange(from = 0.0, to = 1.0) brightness: Float
    ): Int {
        return Color.HSVToColor(0xFF, floatArrayOf(hue, saturation, brightness))
    }

    @ColorInt
    fun HSVToColorWithAlpha(
        @androidx.annotation.IntRange(from = 0, to = 255) alpha: Int,
        @FloatRange(from = 0.0, to = 360.0) hue: Float,
        @FloatRange(from = 0.0, to = 1.0) saturation: Float,
        @FloatRange(from = 0.0, to = 1.0) brightness: Float
    ): Int {
        return Color.HSVToColor(alpha, floatArrayOf(hue, saturation, brightness))
    }
}