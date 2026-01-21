package com.maker.ui.customview

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.maker.base.AbsBaseAdapter
import com.maker.base.AbsBaseDiffCallBack
import com.maker.data.model.BodyPartModel
import com.maker.utils.DataHelper.dp
import com.maker.utils.DataHelper.setMargins
import com.maker.utils.onClickCustom
import com.maker.R
import com.maker.databinding.ItemNavigationBinding

class NavAdapter(context: Context) : AbsBaseAdapter<BodyPartModel, ItemNavigationBinding>(R.layout.item_navigation, DiffNav()) {
    val ct= context
    var posNav = 0
    var onClick: ((Int) -> Unit)? = null

    class DiffNav : AbsBaseDiffCallBack<BodyPartModel>() {
        override fun itemsTheSame(oldItem: BodyPartModel, newItem: BodyPartModel): Boolean {
            return oldItem.icon == newItem.icon
        }

        override fun contentsTheSame(oldItem: BodyPartModel, newItem: BodyPartModel): Boolean {
            return oldItem.icon != newItem.icon
        }

    }

    fun setPos(pos: Int) {
        posNav = pos
    }

    override fun bind(
        binding: ItemNavigationBinding,
        position: Int,
        data: BodyPartModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.mainNav.setMargins(0,15.dp(ct),8.dp(ct), 15.dp(ct))
        Glide.with(binding.root).load(data.icon).encodeQuality(90).override(256).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imv)
        if (posNav == position) {
            binding.mainNav.setMargins(0, 5.dp(ct),8.dp(ct), 25.dp(ct))
            binding.bg.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.FFCC00))
        } else {
            binding.bg.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.white))
        }
        binding.root.onClickCustom {
            onClick?.invoke(position)
        }
    }

}