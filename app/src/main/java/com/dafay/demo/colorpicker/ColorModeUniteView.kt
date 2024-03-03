package com.dafay.demo.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import com.dafay.demo.colorpicker.databinding.LayoutColorModeUniteViewBinding

/**
 * @Des 颜色模式组合视图
 * @Author lipengfei
 * @Date 2024/1/3
 */
class ColorModeUniteView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr) {

    private var _binding: LayoutColorModeUniteViewBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = LayoutColorModeUniteViewBinding.inflate(LayoutInflater.from(context), this, true)
        initViews()
    }

    private var curColorMode: ColorMode = ColorMode.HEX
    var listener: OnColorModeChangeListener? = null

    private fun initViews() {
        binding.mcvColorMode.setOnClickListener {
            showColorModeMenu(binding.tvColorMode)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    @SuppressLint("RestrictedApi")
    private fun showColorModeMenu(v: View) {
        val popup = PopupMenu(v.context, v)
        // Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.color_mode_popup_menu, popup.menu)

        // There is no public API to make icons show on menus.
        // IF you need the icons to show this works however it's discouraged to rely on library only
        // APIs since they might disappear in future versions.
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8f,
                    resources.displayMetrics
                ).toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon = object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                            override fun getIntrinsicWidth(): Int {
                                return intrinsicHeight + iconMarginPx + iconMarginPx
                            }
                        }
                    }
                }
            }
        }
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            return@setOnMenuItemClickListener dealMenuItemClick(menuItem)
        }
        popup.show()
    }

    private fun dealMenuItemClick(menuItem: MenuItem): Boolean {
        val menuItemTitle = menuItem.title.toString()
        binding.tvColorMode.text = menuItemTitle

        val temp = ColorMode.from(menuItemTitle.replace("Picker", "hex"))
        if (temp == curColorMode) {
            return false
        }
        curColorMode = temp
        listener?.onChange(curColorMode)
        when (menuItem.itemId) {
            R.id.rgb -> {
                binding.tietZero.visibility = View.GONE

                binding.tietOne.visibility = View.VISIBLE
                binding.tietTwo.visibility = View.VISIBLE
                binding.tietThree.visibility = View.VISIBLE
                binding.tietFour.visibility = View.VISIBLE
                binding.tietFive.visibility = View.GONE

                binding.mdDividerOne.visibility = View.VISIBLE
                binding.mdDividerTwo.visibility = View.VISIBLE
                binding.mdDividerThree.visibility = View.VISIBLE
                binding.mdDividerFour.visibility = View.GONE

                binding.tietOne.hint = "r"
                binding.tietTwo.hint = "g"
                binding.tietThree.hint = "b"
                binding.tietFour.hint = "a"
            }

            R.id.hsb -> {
                binding.tietZero.visibility = View.GONE

                binding.tietOne.visibility = View.VISIBLE
                binding.tietTwo.visibility = View.VISIBLE
                binding.tietThree.visibility = View.VISIBLE
                binding.tietFour.visibility = View.VISIBLE
                binding.tietFive.visibility = View.GONE

                binding.mdDividerOne.visibility = View.VISIBLE
                binding.mdDividerTwo.visibility = View.VISIBLE
                binding.mdDividerThree.visibility = View.VISIBLE
                binding.mdDividerFour.visibility = View.GONE

                binding.tietOne.hint = "h"
                binding.tietTwo.hint = "s"
                binding.tietThree.hint = "b"
                binding.tietFour.hint = "a"
            }

            R.id.hsl -> {
                binding.tietZero.visibility = View.GONE

                binding.tietOne.visibility = View.VISIBLE
                binding.tietTwo.visibility = View.VISIBLE
                binding.tietThree.visibility = View.VISIBLE
                binding.tietFour.visibility = View.VISIBLE
                binding.tietFive.visibility = View.GONE

                binding.mdDividerOne.visibility = View.VISIBLE
                binding.mdDividerTwo.visibility = View.VISIBLE
                binding.mdDividerThree.visibility = View.VISIBLE
                binding.mdDividerFour.visibility = View.GONE

                binding.tietOne.hint = "h"
                binding.tietTwo.hint = "s"
                binding.tietThree.hint = "l"
                binding.tietFour.hint = "a"
            }

            R.id.cmyk -> {
                binding.tietZero.visibility = View.GONE

                binding.tietOne.visibility = View.VISIBLE
                binding.tietTwo.visibility = View.VISIBLE
                binding.tietThree.visibility = View.VISIBLE
                binding.tietFour.visibility = View.VISIBLE
                binding.tietFive.visibility = View.VISIBLE

                binding.mdDividerOne.visibility = View.VISIBLE
                binding.mdDividerTwo.visibility = View.VISIBLE
                binding.mdDividerThree.visibility = View.VISIBLE
                binding.mdDividerFour.visibility = View.VISIBLE

                binding.tietOne.hint = "c"
                binding.tietTwo.hint = "m"
                binding.tietThree.hint = "y"
                binding.tietFour.hint = "k"
                binding.tietFive.hint = "a"
            }
            // 默认 picker 模式
            else -> {
                binding.tietZero.visibility = View.VISIBLE
                binding.tietZero.hint = "hex"

                binding.tietOne.visibility = View.GONE
                binding.tietTwo.visibility = View.GONE
                binding.tietThree.visibility = View.GONE
                binding.tietFour.visibility = View.GONE
                binding.tietFive.visibility = View.GONE

                binding.mdDividerOne.visibility = View.GONE
                binding.mdDividerTwo.visibility = View.GONE
                binding.mdDividerThree.visibility = View.GONE
                binding.mdDividerFour.visibility = View.GONE

                binding.tietOne.hint = "c"
                binding.tietTwo.hint = "m"
                binding.tietThree.hint = "y"
                binding.tietFour.hint = "k"
                binding.tietFive.hint = "a"
            }
        }
        return true
    }


    fun setPreviewColor(color: Int) {
        binding.ivPreviewColor.setBackgroundColor(color)
    }

    fun setHexText(s: String) {
        binding.tietZero.setText(s)
    }

    interface OnColorModeChangeListener {
        fun onChange(colorMode: ColorMode)
    }

}