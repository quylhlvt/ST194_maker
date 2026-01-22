package com.fokalore.ocmaker.create.ui.customview

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fokalore.ocmaker.create.base.AbsBaseAdapter
import com.fokalore.ocmaker.create.base.AbsBaseDiffCallBack
import com.fokalore.ocmaker.create.data.model.BodyPartModel
import com.fokalore.ocmaker.create.utils.DataHelper.dp
import com.fokalore.ocmaker.create.utils.DataHelper.setMargins
import com.fokalore.ocmaker.create.utils.onClickCustom
import com.fokalore.ocmaker.create.R
import com.fokalore.ocmaker.create.databinding.ItemNavigationBinding

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
        binding.mainNav.setMargins(0,15.dp(ct),8.dp(ct), 5.dp(ct))
        Glide.with(binding.root).load(data.icon).encodeQuality(90).override(256).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imv)
        if (posNav == position) {
            binding.mainNav.setMargins(0, 0,8.dp(ct), 20.dp(ct))
            binding.bg.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.FFCC00))
        } else {
            binding.bg.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.white))
        }
        binding.root.onClickCustom {
            onClick?.invoke(position)
        }
    }

}