package com.hoang.survey.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.Utils
import com.google.gson.JsonSyntaxException
import com.hoang.survey.R
import com.hoang.survey.api.ApiResponse
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import org.koin.core.KoinComponent
import retrofit2.HttpException
import retrofit2.Response
import javax.net.ssl.HttpsURLConnection

open class BaseViewModel : ViewModel(), KoinComponent {
    val apiErrorMessage = SingleLiveEvent<String>()
    val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    val disposables = CompositeDisposable()

    init {
        _loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }

    /**
     * Only convert Retrofit callback to ApiResponse and log exception if thrown
     */
    abstract inner class RawCallbackWrapper<K> : DisposableSingleObserver<K>() {
        protected abstract fun onResponse(response: ApiResponse<K>)

        override fun onStart() {
            _loading.value = true
        }

        override fun onSuccess(t: K) {
            onResponse(ApiResponse(t, null))
            _loading.value = false
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            handleApiError(e)
            _loading.value = false
            onResponse(ApiResponse(null, e))
        }
    }

    /**
     * Process Retrofit callback with auto show/hide loading indicator and error notification
     */
    abstract inner class FullCallbackWrapper<K> : DisposableSingleObserver<K>() {
        protected abstract fun onResponse(response: ApiResponse<K>)
        override fun onStart() {
            _loading.value = true
        }

        override fun onSuccess(t: K) {
            onResponse(ApiResponse(t, null))
            _loading.value = false
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            handleApiError(e)
            _loading.value = false
            onResponse(ApiResponse(null, e))
        }
    }

    fun handleApiError(error: Throwable) {
        if (error is HttpException) {
            when (error.code()) {
                HttpsURLConnection.HTTP_UNAUTHORIZED -> apiErrorMessage.value =
                    Utils.getApp().getString(R.string.unauthorized_user)
                HttpsURLConnection.HTTP_FORBIDDEN -> apiErrorMessage.value =
                    Utils.getApp().getString(R.string.forbidden)
                HttpsURLConnection.HTTP_INTERNAL_ERROR -> apiErrorMessage.value =
                    Utils.getApp().getString(R.string.internal_server_error)
                HttpsURLConnection.HTTP_BAD_REQUEST -> apiErrorMessage.value =
                    Utils.getApp().getString(R.string.bad_request)
                else -> apiErrorMessage.value = error.getLocalizedMessage()
            }
        } else if (error is JsonSyntaxException) {
            apiErrorMessage.value = Utils.getApp().getString(R.string.wrong_data_format)
        } else {
            apiErrorMessage.value = error.message
        }

    }
}