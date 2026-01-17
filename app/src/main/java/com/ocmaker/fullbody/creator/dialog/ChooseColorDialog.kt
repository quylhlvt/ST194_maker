package com.ocmaker.fullbody.creator.dialog

import android.app.Activity
import android.graphics.Color
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.databinding.DialogColorPickerBinding
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.base.BaseDialog


class ChooseColorDialog(context: Activity) : BaseDialog<DialogColorPickerBinding>(context, false) {
    var onDoneEvent: ((Int) -> Unit) = {}
    private var color = Color.WHITE
    override fun getContentView(): Int = R.layout.dialog_color_picker

    override fun initView() {
        binding.apply {
            colorPickerView.apply {
                hueSliderView = hueSlider
            }
            txtColor.post { txtColor.text = String.format("#%06X", 0xFFFFFF and color) }
        }
    }

    override fun bindView() {
        binding.apply {
            colorPickerView.setOnColorChangedListener { newColor -> color = newColor
                txtColor.post { txtColor.text = String.format("#%06X", 0xFFFFFF and color) } }
            btnClose.onSingleClick { dismiss() }
            btnDone.onSingleClick {
                dismiss()
                onDoneEvent.invoke(color)
            }
        }
    }


}