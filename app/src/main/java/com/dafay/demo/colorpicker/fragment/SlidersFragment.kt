package com.dafay.demo.colorpicker.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dafay.demo.colorpicker.ALPHA_COLOR_CHECKPOINTS
import com.dafay.demo.colorpicker.HUE_COLOR_CHECKPOINTS
import com.dafay.demo.colorpicker.databinding.FragmentSlidersBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @Des 选择颜色弹窗
 * @Author lipengfei
 * @Date 2023/12/27
 */
class SlidersFragment : BottomSheetDialogFragment {

    constructor() : super()

    private lateinit var binding: FragmentSlidersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentSlidersBinding.inflate(LayoutInflater.from(requireContext()))
        val bottomSheetDialog = BottomSheetDialog(requireContext(), theme)
        bottomSheetDialog.setContentView(binding.root)
        initViews()
        return bottomSheetDialog
    }


    private fun initViews() {
        binding.sHueSlider.setTrackGradientColor(HUE_COLOR_CHECKPOINTS)
        binding.sHueSlider1.setTrackGradientColor(HUE_COLOR_CHECKPOINTS)
        binding.sHueSlider2.setTrackGradientColor(HUE_COLOR_CHECKPOINTS)

        binding.sAlphaSlider.setTrackGradientColor(ALPHA_COLOR_CHECKPOINTS)
        binding.sAlphaSlider1.setTrackGradientColor(ALPHA_COLOR_CHECKPOINTS)
        binding.sAlphaSlider2.setTrackGradientColor(ALPHA_COLOR_CHECKPOINTS)
    }


}