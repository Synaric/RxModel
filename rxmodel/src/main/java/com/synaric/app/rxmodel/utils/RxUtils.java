package com.synaric.app.rxmodel.utils;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * RxJava工具类。
 * Created by Synaric-雍高超 on 2016/8/23 0023.
 */
public class RxUtils {

    /**
     * 通过Callable创建Observable结果对象。
     * 这个Observable将在{@link Schedulers#io()}线程被调用，在{@link AndroidSchedulers#mainThread()}回调结果。
     */
    public static <T> Observable<T> makeModelObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }
}
