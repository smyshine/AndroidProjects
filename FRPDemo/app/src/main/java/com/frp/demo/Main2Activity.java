package com.frp.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.reactivestreams.Subscription;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

//        testWithCreate();

//        testOperation();

        testPrediction();

    }


    private void testPrediction() {
        //all, for all elements
//        Flowable.just(1, 2, 3, 4)
//                .all(integer -> integer >= 2)
//                .subscribe(integer -> Log.e(TAG, "testPrediction: " + integer));
        //>=1--true, >=2--false

        //ambArray, choose the first element shooter to handle and abandon others
//        Flowable.ambArray(
//                Flowable.timer(1, TimeUnit.SECONDS),
//                Flowable.just(3, 4, 5))
//                .subscribe(integer -> Log.e(TAG, "testPrediction: " + integer));
        //3,4,5

        //scan
        Flowable.just(1, 2, 3)
                .scan((last, item) -> {
                    Log.e(TAG, "last:" + String.valueOf(last));
                    Log.e(TAG, "item:" + String.valueOf(item));
                    return item + 1;
                })
                .subscribe(ele -> Log.e(TAG, String.valueOf(ele)));


    }

    private void testOperation() {
        //just, simply create observable/flowable/etc..
//        Flowable.just("hello", "how", "are", "you")
//                .subscribe(str-> Log.e(TAG, "testOperation: just " + str));
        //hello how are you

        //fromArray, similar as just, difference is that fromArray can pass in any number of source stream

        //empty, directly call complete fun of subscriber

        //error, directly call error fun of subscriber

        //never, invoke nothing, mostly used in test

        //fromIterable, if pass in a list to fromArray, it will be considered as a param, but fromIterable can iterate a list

        //timer, invoke data send on a pointed time lapse

        //interval, invoke data send every pointed time lapse, and will not stop

        //intervalRange, invoke data send every period from pointed x to y and immediately stop

        //range/rangeLong, invoke data send without delay

        //defer, evey observable will create observers again when observed
//        Flowable<String> flowable = Flowable.defer(() -> Flowable.just("hello", "smy"));
//        flowable.subscribe(str -> Log.e(TAG, "testOperation: " + str));
//        flowable.subscribe(str -> Log.e(TAG, "testOperation: " + str));
        //hello smy hello smy


        //filter, set predictor to filter
//        Flowable.just("hello", "world", "smy", "andsmy")
//                .filter(s -> s.length() > 3)
//                .subscribe(s -> Log.e(TAG, "testOperation: " + s));

        //concat/concatArray, invoke by order
//        Flowable.concat(Flowable.intervalRange(0, 3, 1, 1, TimeUnit.SECONDS),
//                Flowable.intervalRange(3, 3, 1, 1, TimeUnit.SECONDS))
//                .subscribe(aLong -> Log.e(TAG, "accept: " + aLong));
        //0,1,2,3,4,5    6s


        //merge/mergeArray, invoke in time-serial order
//        Flowable.merge(Flowable.intervalRange(0, 3, 1, 1, TimeUnit.SECONDS),
//                Flowable.intervalRange(3, 3, 1, 1, TimeUnit.SECONDS))
//                .subscribe(aLong -> Log.e(TAG, "accept: " + aLong));
        //0,3,1,4,2,5   3s

        //zip,
//        Flowable.zip(Flowable.just(1, 2, 3),
//                Flowable.just(4, 5),
//                (int1, int2) -> int1 + int2)
//                .subscribe(integer -> Log.e(TAG, "testOperation: " + integer));
        //5, 7

        //reduce, compose all into single
//        Flowable.just(1, 2, 3)
//        .reduce((last, item) -> {
//            Log.e(TAG, "testOperation: " + last + " , " + item);
//            return last + item;
//        }).subscribe(integer -> Log.e(TAG, "testOperation: "  + integer));
        //1,2; 3,3; 6

        //count
//        Flowable.just("1", "a", "b")
//                .count()
//                .subscribe(aLong -> Log.e(TAG, "testOperation: " + aLong));
        //3

        //collect
//        Flowable.just(1, 2, 3, 4)
//                .collect(ArrayList::new,
//                        (BiConsumer<ArrayList<Integer>, Integer>) ArrayList::add)
//                .subscribe(integers -> Log.e(TAG, "accept: " + integers));
        //[1,2,3,4]
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
