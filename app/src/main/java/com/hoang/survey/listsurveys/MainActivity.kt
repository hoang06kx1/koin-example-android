package com.hoang.survey.listsurveys

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.hoang.survey.R
import com.hoang.survey.base.BaseActivity
import com.hoang.survey.base.ViewExt.Companion.CLICK_THROTTLE_TIME
import com.hoang.survey.base.observeApiErrorMessageFromViewModel
import com.hoang.survey.base.observeLoadingFromViewModel
import com.hoang.survey.base.toastInfoLong
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

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
        mainActivityViewModel.getSurveysLazy()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        pager_surveys.offscreenPageLimit = 3
        pager_surveys.adapter = SurveyPagerAdapter()
        indicator.setViewPager(pager_surveys)
        (pager_surveys.adapter as SurveyPagerAdapter).registerAdapterDataObserver(indicator.adapterDataObserver)
        pager_surveys.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mainActivityViewModel.handleLoadMoreSurveys(position)
            }
        })
        bt_refresh.clicks().throttleFirst(CLICK_THROTTLE_TIME, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe {
            mainActivityViewModel.refreshSurvey()
        }.addTo(disposables)
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
