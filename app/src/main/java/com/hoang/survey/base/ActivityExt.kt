package com.hoang.survey.base

import android.app.Activity
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.hoang.survey.R
import es.dmoral.toasty.Toasty

fun Activity.toastErrorShort(stringId: Int) {
    Toasty.success(this, stringId, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastErrorShort(message: String) {
    Toasty.success(this, message, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastErrorLong(stringId: Int) {
    Toasty.success(this, stringId, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastErrorLong(message: String) {
    Toasty.success(this, message, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastSuccessShort(stringId: Int) {
    Toasty.success(this, stringId, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastSuccessShort(message: String) {
    Toasty.success(this, message, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastSuccessLong(stringId: Int) {
    Toasty.success(this, stringId, Toast.LENGTH_LONG, true).show()
}

fun Activity.toastSuccessLong(message: String) {
    Toasty.success(this, message, Toast.LENGTH_LONG, true).show()
}

fun Activity.toastInfoShort(stringId: Int) {
    Toasty.info(this, stringId, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastInfoShort(message: String) {
    Toasty.info(this, message, Toast.LENGTH_SHORT, true).show()
}

fun Activity.toastInfoLong(stringId: Int) {
    Toasty.info(this, stringId, Toast.LENGTH_LONG, true).show()
}

fun Activity.toastInfoLong(message: String) {
    Toasty.info(this, message, Toast.LENGTH_LONG, true).show()
}

fun BaseActivity.observeLoadingFromViewModel(viewModel: BaseViewModel) {
    viewModel.loading.observe(this, Observer { isLoading ->
        if (isLoading == true)
            showLoadingDialog()
        else
            hideLoadingDialog()
    })
}

fun BaseActivity.observeApiErrorMessageFromViewModel(viewModel: BaseViewModel) {
    viewModel.apiErrorMessage.observe(this, Observer { message ->
        toastErrorLong(message)
    })
}

fun ImageView.loadImage(url: String, errorPlaceHolder: Int? = null) {
    if (this.context is Fragment || this.context is Activity)
        if (errorPlaceHolder != null) {
            Glide.with(this.context).load(url).error(errorPlaceHolder).into(this)
        } else {
            Glide.with(this.context).load(url).into(this)
        }
}