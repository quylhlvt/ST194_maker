package com.maker.ui.setting

import android.view.View
import com.maker.base.AbsBaseActivity
import com.maker.ui.language.LanguageActivity
import com.maker.utils.RATE
import com.maker.utils.SharedPreferenceUtils
import com.maker.utils.newIntent
import com.maker.utils.onSingleClick
import com.maker.utils.policy
import com.maker.utils.rateUs
import com.maker.utils.shareApp
import com.maker.utils.unItem
import com.maker.R
import com.maker.databinding.ActivitySettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AbsBaseActivity<ActivitySettingBinding>() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceUtils
    override fun getLayoutId(): Int = R.layout.activity_setting

    override fun initView() {
        binding.titleSetting.isSelected = true
        if (sharedPreferences.getBooleanValue(RATE)) {
            binding.llRateUs.visibility = View.GONE
        }
        unItem = {
            binding.llRateUs.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
    }
    override fun initAction() {
        binding.apply {
            llLanguage.onSingleClick {
                startActivity(
                    newIntent(
                        applicationContext,
                        LanguageActivity::class.java
                    )
                )
            }
            llRateUs.onSingleClick {
                rateUs(0)
            }
            llShareApp.onSingleClick {
                shareApp()
            }
            llPrivacy.onSingleClick {
                policy()
            }
            imvBack.onSingleClick {
                finish()
            }
        }
    }
}