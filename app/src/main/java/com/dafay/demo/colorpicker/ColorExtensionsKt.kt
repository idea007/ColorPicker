@file:OptIn(ExperimentalStdlibApi::class)

package com.example.demo.live.wallpaper.utils

import android.content.res.Resources
import android.graphics.Color
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red



/**
 * return 00000000
 */
val Int.toAlphaString: String
    get() = Color.alpha(this).toHexString()

val Int.toRedString: String
    get() = Color.red(this).toHexString()

val Int.toGreenString: String
    get() = Color.green(this).toHexString()

val Int.toBlueString: String
    get() = Color.blue(this).toHexString()

val Int.toHexRGB: String
    get() = String.format("#%02x%02x%02x", this.red, this.green, this.blue)

val Int.toHexARGB: String
    get() = String.format("#%02x%02x%02x%02x", this.alpha, this.red, this.green, this.blue)
