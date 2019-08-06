package com.hoang.survey.listsurveys

import android.os.Bundle
import androidx.lifecycle.Observer
import com.hoang.survey.R
import com.hoang.survey.base.BaseActivity
import com.hoang.survey.base.observeApiErrorMessageFromViewModel
import com.hoang.survey.base.observeLoadingFromViewModel
import com.hoang.survey.base.toastInfoLong
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {
    val mainActivityViewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        observeLoadingFromViewModel(mainActivityViewModel)
        observeApiErrorMessageFromViewModel(mainActivityViewModel)
        mainActivityViewModel.surveysLiveData.observe(this, Observer {
            (pager_surveys.adapter as SurveyPagerAdapter).submitData(it)
        })
        mainActivityViewModel.getSurveys(1,5)
    }

    private fun initViews() {
        pager_surveys.offscreenPageLimit = 3
        pager_surveys.adapter = SurveyPagerAdapter()
    }

    var lastBackPressTime = 0L
    var shouldExit = false
    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressTime > 500) {
            if (supportFragmentManager.backStackEntryCount == 0 && isTaskRoot) {
                toastInfoLong(R.string.back_again_to_exit)
                shouldExit = true
            } else {
                shouldExit = false
            }
        } else {
            if (shouldExit) super.onBackPressed()
        }
        lastBackPressTime = System.currentTimeMillis()
    }
}
