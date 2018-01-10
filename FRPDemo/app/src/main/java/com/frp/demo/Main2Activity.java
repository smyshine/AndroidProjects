package com.frp.demo;

import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Collections;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

//        testWithFrom();

        testWithCreate();

    }

    private void testWithCreate() {
        //create observer
        FlowableSubscriber<String> subscriber = new FlowableSubscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, "onNext: of subscriber " + s );
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "onError: of subsriber " + t.toString());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: of subsriber ");
            }
        };

        //create object, thus to be observed
        Flowable<String> flowable = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                emitter.onNext("hello");
                emitter.onNext("world");
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);

        //combine two
//        flowable.subscribe(subscriber);

        //many times we don't care about complete
        flowable.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e(TAG, "accept: consumer " + s);
            }
        });


        //use observable instead of flowable, 因为observable不支持订阅Subsrciber，所以使用observer来订阅
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("hello");
                emitter.onNext("world");
                emitter.onComplete();
            }
        });

        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.e(TAG, "onSubscribe: observer");
            }

            @Override
            public void onNext(String s) {
                Log.e(TAG, "onNext: observer " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: observer " + e.toString());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: observer ");
            }
        };

        observable.subscribe(observer);


        //single observer

        //completable observer

        //maybe observer
        Maybe<String> maybe = Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> emitter) throws Exception {
                emitter.onSuccess("maybe success");//发送一个数据or onError
//                emitter.onComplete();//不发送数据的情况或者onError
            }
        });

        maybe.subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "onSuccess: maybe observer " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: maybe observer " + e.toString());
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: maybe observer ");

            }
        });

//        Action<>                  //no param
//        Consumer<T>               //one param
//        BiConsumer<T1, T2>        //two params
//        Consumer<T1, T2, T3...>   //many params
    }

    private void testWithFrom() {
        Observable<String> sentenceObservable = Observable.fromArray("this", "is", "a", "test");

        sentenceObservable.map(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                return s.toUpperCase() + " ";
            }
        })
                .toList()
                .map(new Function<List<String>, String>() {
                    @Override
                    public String apply(List<String> strings) throws Exception {
                        Collections.reverse(strings);
                        return strings.toString();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {

                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "accept: " + s);
                    }
                });
    }

}
