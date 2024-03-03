package com.dafay.demo.colorpicker.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dafay.demo.colorpicker.ALPHA_COLOR_CHECKPOINTS
import com.dafay.demo.colorpicker.HUE_COLOR_CHECKPOINTS
import com.dafay.demo.colorpicker.MColorConvertUtils
import com.dafay.demo.colorpicker.SaturationEditView
import com.dafay.demo.colorpicker.databinding.FragmentColorpickerBinding
import com.dafay.demo.colorpicker.databinding.FragmentSlidersBinding
import com.dafay.demo.lib.base.utils.debug
import com.dafay.demo.lib.widget.slider.Slider
import com.example.demo.live.wallpaper.utils.toHexARGB
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @Des 选择颜色弹窗
 * @Author lipengfei
 * @Date 2023/12/27
 */
class ColorPickerFragment : BottomSheetDialogFragment {

    constructor() : super()

    private lateinit var binding: FragmentColorpickerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentColorpickerBinding.inflate(LayoutInflater.from(requireContext()))
        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)
        bottomSheetDialog.setContentView(binding.root)
        initViews()
        return bottomSheetDialog
    }


    private fun initViews() {
        binding.sSliderHue.setTrackGradientColor(HUE_COLOR_CHECKPOINTS)
        binding.sSliderHue.valueTo = 360f
        binding.sSliderHue.value = 0f
        binding.sSliderHue.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
               debug("value=${value}")
                binding.svSaturation.updateHue(value)

                var alpha = (binding.sSliderAlpha.value * 255).toInt()
                var color = MColorConvertUtils.HSVToColorWithAlpha(
                    alpha,
                    value,
                    binding.svSaturation.getSaturation(),
                    binding.svSaturation.getBrightness()
                )
                binding.uvColorMode.setPreviewColor(color)
                binding.uvColorMode.setHexText(color.toHexARGB)
            }
        })

        binding.sSliderAlpha.setTrackGradientColor(ALPHA_COLOR_CHECKPOINTS)
        binding.sSliderAlpha.value = 1f
        binding.sSliderAlpha.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                var alpha = (binding.sSliderAlpha.value * 255).toInt()
                var color = MColorConvertUtils.HSVToColorWithAlpha(
                    alpha,
                    binding.svSaturation.getHub(),
                    binding.svSaturation.getSaturation(),
                    binding.svSaturation.getBrightness()
                )
                binding.uvColorMode.setPreviewColor(color)
                binding.uvColorMode.setHexText(color.toHexARGB)
            }
        })

        binding.svSaturation.listener = object : SaturationEditView.OnHsbChangedListener {
            override fun hsbChanged(h: Float, s: Float, b: Float) {
                debug("h=${h} s=$s b=$b")
                var alpha = (binding.sSliderAlpha.value * 255).toInt()
                var color = MColorConvertUtils.HSVToColorWithAlpha(alpha, h, s, b)
                binding.uvColorMode.setPreviewColor(color)
                binding.uvColorMode.setHexText(color.toHexARGB)
            }
        }
    }


}