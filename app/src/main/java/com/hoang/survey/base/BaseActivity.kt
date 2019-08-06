package com.hoang.survey.base

import androidx.appcompat.app.AppCompatActivity
import com.kaopiz.kprogresshud.KProgressHUD
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : AppCompatActivity() {
    private var loadingDialog: KProgressHUD? = null
    protected var disposables = CompositeDisposable()

    fun showLoadingDialog() {
        if (!isFinishing) {
            if (loadingDialog == null) {
                loadingDialog = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setCancellable(false)
                    .setDimAmount(0.5f)
//                loadingDialog?.setTitleText(getString(R.string.please_wait))?.setCancelable(false)
            }

            if (!isShowingLoadingDialog()) {
                loadingDialog?.show()
            }
        }
    }

    fun hideLoadingDialog() {
        if (isShowingLoadingDialog()) {
            loadingDialog?.dismiss()
        }
    }

    fun isShowingLoadingDialog(): Boolean {
        return loadingDialog?.isShowing == true
    }

    override fun onPause() {
        super.onPause()
        hideLoadingDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }
}