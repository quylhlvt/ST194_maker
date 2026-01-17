package com.ocmaker.fullbody.creator.ui.quick_mix

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.databinding.ItemMixBinding
import com.ocmaker.fullbody.creator.dialog.DialogExit
import com.ocmaker.fullbody.creator.utils.DataHelper
import com.ocmaker.fullbody.creator.utils.hide
import com.ocmaker.fullbody.creator.utils.isInternetAvailable
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.utils.show
import com.ocmaker.fullbody.creator.utils.showToast
import com.ocmaker.fullbody.creator.base.AbsBaseAdapter
import com.ocmaker.fullbody.creator.base.AbsBaseDiffCallBack
import com.ocmaker.fullbody.creator.data.model.CustomModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickAdapter(activity: Activity) : AbsBaseAdapter<CustomModel, ItemMixBinding>(
    R.layout.item_mix, DiffCallBack()
) {
    var act = activity
    var arrListImageSortView = arrayListOf<ArrayList<String>>()
    val arrBitmap = hashMapOf<Int, Bitmap>()
    var onCLick: ((Int) -> Unit)? = null
    var listArrayInt = arrayListOf<ArrayList<ArrayList<Int>>>()
    override fun bind(
        binding: ItemMixBinding,
        position: Int,
        data: CustomModel,
        holder: RecyclerView.ViewHolder
    ) {
        // ✅ Tag để track position hiện tại
        binding.imvImage.tag = position

        if (!arrBitmap.containsKey(position)){
            // ✅ Chưa có cache - Clear ảnh cũ và cancel request
            Glide.with(binding.root.context).clear(binding.imvImage)
            binding.imvImage.setImageDrawable(null)
            binding.shimmer.startShimmer()
            binding.shimmer.show()

            binding.shimmer.onSingleClick {
                if (!isInternetAvailable(this.act)){
                    DialogExit(this.act, "network").show()
                    return@onSingleClick
                }
                showToast(
                    binding.root.context,
                    R.string.wait_a_few_second
                )
            }

            val coordSet = listArrayInt[position]
            mergeImages(binding.root.context, "", data, arrListImageSortView[position % DataHelper.arrBlackCentered.size], coordSet) { mergedBitmap ->
                // ✅ Kiểm tra ImageView vẫn đang hiển thị position này không
                if (binding.imvImage.tag == position) {
                    binding.shimmer.stopShimmer()
                    binding.shimmer.hide()
                    Glide.with(binding.root.context)
                        .load(mergedBitmap)
                        .encodeQuality(80)
                        .override(256)
                        .into(binding.imvImage)
                    arrBitmap[position] = mergedBitmap
                }
            }

            binding.root.onSingleClick { onCLick?.invoke(position) }
        } else {
            // ✅ Đã có cache - KHÔNG clear, load trực tiếp
            binding.shimmer.stopShimmer()
            binding.shimmer.hide()

            // ✅ QUAN TRỌNG: Dùng placeholder để tránh hiển thị ảnh cũ
            Glide.with(binding.root.context)
                .load(arrBitmap[position])
                .placeholder(binding.imvImage.drawable) // Giữ ảnh hiện tại nếu có
                .encodeQuality(80)
                .override(256)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // ✅ Không cache vì đã có arrBitmap
                .into(binding.imvImage)

            binding.root.onSingleClick { onCLick?.invoke(position) }
        }

        binding.imvImage.onSingleClick {
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
    private fun mergeImages(
        context: Context,
        bgRes: String,
        blackCentered: CustomModel,
        listImageSortView: List<String>,
        coordSet: ArrayList<ArrayList<Int>>,
        onDone: (Bitmap) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val canvasSize = 256
                val merged = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(merged)

                val dstRect = RectF(
                    0f,
                    0f,
                    canvasSize.toFloat(),
                    canvasSize.toFloat()
                )
                // 1️⃣ Load ảnh nền
//                val bgBitmap = Glide.with(context)
//                    .asBitmap()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .load(bgRes)
//                    .submit()
//                    .get()

                // 2️⃣ Tạo bitmap gộp mới

                // 3️⃣ Duyệt từng layer
                listImageSortView.forEachIndexed { index, icon ->
                    val coord = coordSet[index]
                    if (coord[0] > 0) {
                        val targetPath = blackCentered.bodyPart
                            .find { it.icon == icon }
                            ?.listPath?.getOrNull(coord[1])
                            ?.listPath?.getOrNull(coord[0])

                        if (!targetPath.isNullOrEmpty()) {
                            val layerBitmap = Glide.with(context)
                                .asBitmap()
                                .encodeQuality(80)
                                .override(256)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .load(targetPath)
                                .submit()
                                .get()

                            val srcRect = Rect(
                                0,
                                0,
                                layerBitmap.width,
                                layerBitmap.height
                            )
                            // scale về chung 512x512
                            canvas.drawBitmap(layerBitmap, srcRect, dstRect, null)
                        }
                    }
                }

//                bgBitmap.recycle()

                withContext(Dispatchers.Main) {
                    onDone(merged)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}