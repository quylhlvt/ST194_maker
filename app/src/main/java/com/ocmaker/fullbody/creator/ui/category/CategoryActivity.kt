package com.ocmaker.fullbody.creator.ui.category

import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.base.AbsBaseActivity
import com.ocmaker.fullbody.creator.databinding.ActivityCategoryBinding
import com.ocmaker.fullbody.creator.dialog.DialogExit
import com.ocmaker.fullbody.creator.ui.customview.CustomviewActivity
import com.ocmaker.fullbody.creator.utils.DataHelper
import com.ocmaker.fullbody.creator.utils.isInternetAvailable
import com.ocmaker.fullbody.creator.utils.newIntent
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.data.repository.ApiRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : AbsBaseActivity<ActivityCategoryBinding>() {
    @Inject
    lateinit var apiRepository: ApiRepository
    val adapter by lazy { CategoryAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_category



    override fun onRestart() {
        super.onRestart()
    }

    override fun initView() {
        if (DataHelper.arrBlackCentered.size<2 && !isInternetAvailable(this@CategoryActivity)) {
            DialogExit(
                this@CategoryActivity,
                "awaitdataHome"
            ).show()
        }

        if (DataHelper.arrBg.size == 0) {
//            GlobalScope.launch(Dispatchers.IO) {
//                getData(apiRepository)
//            }
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter
            adapter.submitList(DataHelper.arrBlackCentered)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick {
                    finish()
                }

            adapter.onCLick = {
                if (DataHelper.arrBlackCentered[it].checkDataOnline) {
                    if (isInternetAvailable(this@CategoryActivity)) {
                            var a = DataHelper.arrBlackCentered[it].avt.split("/")
                            var b = a[a.size - 1]

                            startActivity(
                                newIntent(
                                    applicationContext,
                                    CustomviewActivity::class.java
                                ).putExtra("data", it)
                            )

                    } else {
                             DialogExit(
                                 this@CategoryActivity,
                                 "network"
                             ).show()
                    }
                } else {
                        var a = DataHelper.arrBlackCentered[it].avt.split("/")
                        var b = a[a.size - 1]

                        startActivity(
                            newIntent(
                                applicationContext,
                                CustomviewActivity::class.java
                            ).putExtra("data", it)
                        )

                }
            }
        }
    }
}