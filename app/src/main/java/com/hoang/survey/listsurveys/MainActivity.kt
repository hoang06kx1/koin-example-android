package com.hoang.survey.listsurveys

import android.os.Bundle
import com.hoang.survey.R
import com.hoang.survey.base.BaseActivity
import com.hoang.survey.base.observeApiErrorMessageFromViewModel
import com.hoang.survey.base.observeLoadingFromViewModel
import com.hoang.survey.base.toastInfoLong
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {
    val mainActivityViewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeLoadingFromViewModel(mainActivityViewModel)
        observeApiErrorMessageFromViewModel(mainActivityViewModel)
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
