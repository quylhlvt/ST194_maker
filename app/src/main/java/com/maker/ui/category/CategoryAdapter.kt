package com.maker.ui.category

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerDrawable
import com.maker.base.AbsBaseAdapter
import com.maker.base.AbsBaseDiffCallBack
import com.maker.data.model.CustomModel
import com.maker.utils.onSingleClick
import com.maker.utils.shimmer
import com.maker.R
import com.maker.databinding.ItemCategoryBinding

class CategoryAdapter : AbsBaseAdapter<CustomModel, ItemCategoryBinding>(
    R.layout.item_category, DiffCallBack()
) {
    var onCLick: ((Int) -> Unit)? = null
    override fun bind(
        binding: ItemCategoryBinding,
        position: Int,
        data: CustomModel,
        holder: RecyclerView.ViewHolder
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
        Glide.with(binding.root).load(data.avt).encodeQuality(70).diskCacheStrategy(
            DiskCacheStrategy.AUTOMATIC).placeholder(shimmerDrawable).into(binding.imv)
        binding.imv.onSingleClick {
            onCLick?.invoke(position)
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<CustomModel>() {
        override fun itemsTheSame(
            oldItem: CustomModel, newItem: CustomModel
        ): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun contentsTheSame(
            oldItem: CustomModel, newItem: CustomModel
        ): Boolean {
            return oldItem.avt != newItem.avt
        }

    }
}