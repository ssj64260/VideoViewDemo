package com.android.vedioviewdemo.service;

import com.orhanobut.logger.Logger;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;


/**
 * 请求回调，处理错误码
 */

public abstract class MyObserver<T> implements Observer<T> {

    private static final String RESULT_CODE_SUCCESS = "0";//成功

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T result) {
        onSuccess(result);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        onError(e.getMessage());
    }

    @Override
    public void onComplete() {

    }

    public void onError(String errorMsg) {
        Logger.e(errorMsg);
    }

    public abstract void onSuccess(@NonNull T result);
}
