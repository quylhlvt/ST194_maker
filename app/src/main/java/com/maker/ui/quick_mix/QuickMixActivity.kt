package com.maker.ui.quick_mix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maker.R
import com.maker.base.AbsBaseActivity
import com.maker.data.model.CustomModel
import com.maker.data.repository.ApiRepository
import com.maker.databinding.ActivityQuickMixBinding
import com.maker.dialog.DialogExit
import com.maker.ui.customview.CustomviewActivity
import com.maker.utils.DataHelper
import com.maker.utils.isInternetAvailable
import com.maker.utils.newIntent
import com.maker.utils.onSingleClick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class QuickMixActivity : AbsBaseActivity<ActivityQuickMixBinding>() {
    private var sizeMix = 100
    private var isLoading = false

    private val arrMix = arrayListOf<CustomModel>()

    @Inject
    lateinit var apiRepository: ApiRepository

    val adapter by lazy { QuickAdapter(this@QuickMixActivity) }

    @Volatile
    private var isNetworkAvailable = true

    @Volatile
    var isOfflineMode = false

    // ✅ Cache danh sách nhân vật offline
    private var offlineModels: List<CustomModel> = emptyList()

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val wasAvailable = isNetworkAvailable
            isNetworkAvailable = isInternetAvailable(this@QuickMixActivity)

            adapter.isNetworkAvailable = isNetworkAvailable

            if (wasAvailable && !isNetworkAvailable) {
                lifecycleScope.launch(Dispatchers.Main) {
                    switchToOfflineMode()
                }
            } else if (!wasAvailable && isNetworkAvailable) {
                lifecycleScope.launch(Dispatchers.Main) {
                    switchToOnlineMode()
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_quick_mix

    override fun onRestart() {
        super.onRestart()
    }

    override fun initView() {
        binding.titleQuick.isSelected = true

        registerNetworkReceiver()
        isNetworkAvailable = isInternetAvailable(this@QuickMixActivity)

        adapter.isNetworkAvailable = isNetworkAvailable

        // ✅ Lọc nhân vật offline ngay từ đầu
        offlineModels = DataHelper.arrBlackCentered.filter { !it.checkDataOnline }

        if (DataHelper.arrBg.isEmpty()) {
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter

            binding.rcv.setHasFixedSize(true)
            binding.rcv.setItemViewCacheSize(12)

            binding.rcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    updateVisibleRange()
                }
            })

            if (!isNetworkAvailable) {
                isOfflineMode = true
                adapter.isOfflineMode = true
                sizeMix = 50 // ✅ Tăng số lượng
                loadOfflineCharacters()
            } else {
                isOfflineMode = false
                adapter.isOfflineMode = false
                sizeMix = 100
                loadAllItems()
            }
        }
    }

    private fun updateVisibleRange() {
        val layoutManager = binding.rcv.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        if (firstVisible != RecyclerView.NO_POSITION) {
            adapter.updateVisibleRange(firstVisible, lastVisible)
        }
    }

    private fun registerNetworkReceiver() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    private suspend fun switchToOnlineMode() {
        if (!isOfflineMode) return

        withContext(Dispatchers.Main) {
            adapter.clearCache()

            arrMix.clear()
            adapter.arrListImageSortView.clear()
            adapter.listArrayInt.clear()

            isOfflineMode = false
            adapter.isOfflineMode = false
            sizeMix = 100

            loadAllItems()
        }
    }

    private suspend fun switchToOfflineMode() {
        if (isOfflineMode) return

        withContext(Dispatchers.Main) {
            adapter.clearCache()

            arrMix.clear()
            adapter.arrListImageSortView.clear()
            adapter.listArrayInt.clear()

            isOfflineMode = true
            adapter.isOfflineMode = true
            sizeMix = 50

            loadOfflineCharacters()

            adapter.submitList(ArrayList(arrMix))
            adapter.notifyDataSetChanged()

            DialogExit(this@QuickMixActivity, "network").show()
        }
    }

    // ✅ LOAD TOÀN BỘ NHÂN VẬT OFFLINE
    private fun loadOfflineCharacters() {
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch(Dispatchers.Default) {
            if (offlineModels.isEmpty()) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    // Không có nhân vật offline
                    finish()
                }
                return@launch
            }

            val tempArrMix = arrayListOf<CustomModel>()
            val tempArrListImageSortView = mutableListOf<ArrayList<String>>()
            val resultList = mutableListOf<ArrayList<ArrayList<Int>>>()

            // ✅ Tạo sizeMix items từ TẤT CẢ các nhân vật offline
            for (pos in 0 until sizeMix) {
                val currentModel = offlineModels[pos % offlineModels.size]

                val list = ArrayList<String>().apply {
                    repeat(currentModel.bodyPart.size) { add("") }
                }

                currentModel.bodyPart.forEach {
                    val (x, _) = it.icon.substringBeforeLast("/")
                        .substringAfterLast("/")
                        .split("-")
                        .map { it.toInt() }
                    list[x - 1] = it.icon
                }
                tempArrListImageSortView.add(list)

                val i = arrayListOf<ArrayList<Int>>()
                list.forEach { data ->
                    val x = currentModel.bodyPart.find { it.icon == data }
                    val pair = if (x != null) {
                        val path = x.listPath[0].listPath
                        val color = x.listPath
                        val randomValue = if (path[0] == "none") {
                            if (path.size > 3) (2 until path.size).random() else 2
                        } else {
                            if (path.size > 2) (1 until path.size).random() else 1
                        }
                        val randomColor = (0 until color.size).random()
                        arrayListOf(randomValue, randomColor)
                    } else {
                        arrayListOf(-1, -1)
                    }
                    i.add(pair)
                }
                resultList.add(i)
                tempArrMix.add(currentModel)
            }

            withContext(Dispatchers.Main) {
                adapter.arrListImageSortView.addAll(tempArrListImageSortView)
                adapter.listArrayInt.addAll(resultList)
                arrMix.addAll(tempArrMix)
                adapter.submitList(ArrayList(arrMix))
                isLoading = false

                updateVisibleRange()
            }
        }
    }

    private fun loadAllItems() {
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch(Dispatchers.Default) {
            val tempArrMix = arrayListOf<CustomModel>()
            val tempArrListImageSortView = mutableListOf<ArrayList<String>>()
            val resultList = mutableListOf<ArrayList<ArrayList<Int>>>()

            for (pos in 0 until sizeMix) {
                val mModel = DataHelper.arrBlackCentered[pos % DataHelper.arrBlackCentered.size]

                val list = ArrayList<String>().apply {
                    repeat(mModel.bodyPart.size) { add("") }
                }
                mModel.bodyPart.forEach {
                    val (x, _) = it.icon.substringBeforeLast("/")
                        .substringAfterLast("/")
                        .split("-")
                        .map { it.toInt() }
                    list[x - 1] = it.icon
                }
                tempArrListImageSortView.add(list)

                val i = arrayListOf<ArrayList<Int>>()
                list.forEach { data ->
                    val x = mModel.bodyPart.find { it.icon == data }
                    val pair = if (x != null) {
                        val path = x.listPath[0].listPath
                        val color = x.listPath
                        val randomValue = if (path[0] == "none") {
                            if (path.size > 3) (2 until path.size).random() else 2
                        } else {
                            if (path.size > 2) (1 until path.size).random() else 1
                        }
                        val randomColor = (0 until color.size).random()
                        arrayListOf(randomValue, randomColor)
                    } else {
                        arrayListOf(-1, -1)
                    }
                    i.add(pair)
                }
                resultList.add(i)
                tempArrMix.add(mModel)
            }

            withContext(Dispatchers.Main) {
                adapter.arrListImageSortView.addAll(tempArrListImageSortView)
                adapter.listArrayInt.addAll(resultList)
                arrMix.addAll(tempArrMix)
                adapter.submitList(ArrayList(arrMix))
                isLoading = false

                updateVisibleRange()
            }
        }
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick { finish() }
            adapter.onCLick = { position ->
                if (isOfflineMode) {
                    // ✅ Tính index nhân vật offline
                    val offlineIndex = position % offlineModels.size
                    val selectedModel = offlineModels[offlineIndex]

                    // ✅ Tìm index thực trong DataHelper.arrBlackCentered
                    val actualIndex = DataHelper.arrBlackCentered.indexOf(selectedModel)

                    if (actualIndex != -1) {
                        startActivity(
                            newIntent(this@QuickMixActivity, CustomviewActivity::class.java)
                                .putExtra("data", actualIndex)
                                .putExtra("arr", adapter.listArrayInt[position])
                        )
                    }
                } else {
                    val index = position % DataHelper.arrBlackCentered.size
                    val model = DataHelper.arrBlackCentered[index]

                    if (model.checkDataOnline) {
                        if (isInternetAvailable(this@QuickMixActivity)) {
                            startActivity(
                                newIntent(this@QuickMixActivity, CustomviewActivity::class.java)
                                    .putExtra("data", index)
                                    .putExtra("arr", adapter.listArrayInt[position])
                            )
                        } else {
                            DialogExit(this@QuickMixActivity, "network").show()
                        }
                    } else {
                        startActivity(
                            newIntent(this@QuickMixActivity, CustomviewActivity::class.java)
                                .putExtra("data", index)
                                .putExtra("arr", adapter.listArrayInt[position])
                        )
                    }
                }
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            unregisterReceiver(networkReceiver)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        adapter.clearCache()
//    }
}