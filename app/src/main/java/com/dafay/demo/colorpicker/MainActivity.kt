package com.dafay.demo.colorpicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.dafay.demo.colorpicker.databinding.ActivityMainBinding
import com.dafay.demo.colorpicker.fragment.ColorPickerFragment
import com.dafay.demo.colorpicker.fragment.SlidersFragment
import com.dafay.demo.lib.base.ui.base.BaseActivity

class MainActivity : BaseActivity(R.layout.activity_main) {
    override val binding: ActivityMainBinding by viewBinding()

    override fun bindListener() {

        binding.btnSlider.setOnClickListener {
            SlidersFragment().show(supportFragmentManager, "")
        }
        binding.btnColorpicker.setOnClickListener {
            ColorPickerFragment().show(supportFragmentManager, "")
        }

    }

}