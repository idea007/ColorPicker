package com.dafay.demo.colorpicker

/**
 * @Des
 * @Author lipengfei
 * @Date 2024/1/8
 */
enum class ColorMode(val title:String) {
    HEX("hex"),
    RGB("rgb"),
    HSB("hsb"),
    HSL("hsl"),
    CMYK("cmyk");

    companion object {
        fun from(title: String?): ColorMode = ColorMode.values().firstOrNull { it.title.equals(title?.lowercase()) } ?: HEX
    }
}