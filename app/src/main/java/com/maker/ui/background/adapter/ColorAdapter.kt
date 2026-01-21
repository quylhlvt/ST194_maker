package com.maker.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.maker.base.AbsBaseAdapter
import com.maker.base.AbsBaseDiffCallBack
import com.maker.data.model.SelectedModel
import com.maker.utils.hide
import com.maker.utils.onSingleClick
import com.maker.utils.show
import com.maker.R
import com.maker.databinding.ItemColorBgBinding

class ColorAdapter :
    AbsBaseAdapter<SelectedModel, ItemColorBgBinding>(R.layout.item_color_bg, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = -1
    override fun bind(
        binding: ItemColorBgBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imvColor.onSingleClick {
            onClick?.invoke(position)
        }
        if(position==0){
            binding.imvColor.setBackgroundResource(R.drawable.imv_add_color)
        }else{
            binding.imvColor.setBackgroundColor(data.color)
        }
        if (data.isSelected) {
            binding.vFocus1.show()
        } else {
            binding.vFocus1.hide()
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<SelectedModel>() {
        override fun itemsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem.path != newItem.path || oldItem.isSelected != newItem.isSelected
        }

    }
}