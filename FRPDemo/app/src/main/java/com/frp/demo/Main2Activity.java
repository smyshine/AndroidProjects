package com.frp.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import org.reactivestreams.Subscription;

import java.io.IOException;
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
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

//        testWithFrom();

//        testWithCreate();

//        testOperation();

//        testPrediction();

//        testRetrofit();

        testRetrofitWithTranslate();

    }

    interface TranslateService {
        //金山
        // URL模板
//        http://fy.iciba.com/ajax.php
        // URL实例
//        http://fy.iciba.com/ajax.php?a=fy&f=auto&t=auto&w=hello%20world

        // 参数说明：
        // a：固定值 fy
        // f：原文内容类型，日语取 ja，中文取 zh，英语取 en，韩语取 ko，德语取 de，西班牙语取 es，法语取 fr，自动则取 auto
        // t：译文内容类型，日语取 ja，中文取 zh，英语取 en，韩语取 ko，德语取 de，西班牙语取 es，法语取 fr，自动则取 auto
        // w：查询内容
        @GET("ajax.php?a=fy&f=auto&t=auto&w=hello%20world")
        Call<CibaTranslation> translate();
    }

    class CibaTranslation {
        private int status;
        content content;

        String show() {
            return "status:" + status +
                    ", content:from=" + content.from +
                    ", to=" + content.to +
                    ", out=" + content.out +
                    ", err=" + content.errNo;
        }
    }

    private static class content {
        private String from;
        private String to;
        private String vendor;
        private String out;
        private int errNo;
    }

    class YouDaoTranslation {
        private String type;
        private int errorCode;
        private int elapsedTime;
        private List<List<TranslateResultBean>> translateResult;

        String show() {
            return "time:" + elapsedTime +
                    ", content:from=" + translateResult.get(0).get(0).getSrc() +
                    ", to=" + translateResult.get(0).get(0).getTgt();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public int getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(int elapsedTime) {
            this.elapsedTime = elapsedTime;
        }

        public List<List<TranslateResultBean>> getTranslateResult() {
            return translateResult;
        }

        public void setTranslateResult(List<List<TranslateResultBean>> translateResult) {
            this.translateResult = translateResult;
        }

    }
    public static class TranslateResultBean {
        /**
         * src : merry me
         * tgt : 我快乐
         */

        public String src;
        public String tgt;

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getTgt() {
            return tgt;
        }

        public void setTgt(String tgt) {
            this.tgt = tgt;
        }
    }

    interface YouDaoService {
        @POST("translate?doctype=json&jsonversion=&type=&keyfrom=&model=&mid=&imei=&vendor=&screen=&ssid=&network=&abtest=")
        @FormUrlEncoded
        Call<YouDaoTranslation> translate(@Field("i") String target);
    }

    private void testRetrofitWithTranslate() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://fy.iciba.com/")  //baseUrl must end in /
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        TranslateService service = retrofit.create(TranslateService.class);
//        Call<CibaTranslation> call = service.translate();
//
//        call.enqueue(new Callback<CibaTranslation>() {
//            @Override
//            public void onResponse(Call<CibaTranslation> call, retrofit2.Response<CibaTranslation> response) {
//                Log.e(TAG, "onResponse: " + response.body().show());
//            }
//
//            @Override
//            public void onFailure(Call<CibaTranslation> call, Throwable t) {
//
//            }
//        });
        // use ResponseBody as result: onResponse: {"status":1,"content":{"from":"en-EU","to":"zh-CN","out":"\u793a\u4f8b","vendor":"ciba","err_no":0}}
        // use CibaTranslation as result:  onResponse: status:1, content:from=en-EU, to=zh-CN, out=示例, err=0
        //          change hello to love:  onResponse: status:1, content:from=en-EU, to=zh-CN, out=爱的世界<br/>, err=0


        new Retrofit.Builder()
                .baseUrl("http://fanyi.youdao.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YouDaoService.class)
                .translate("Happy New Year")
                .enqueue(new Callback<YouDaoTranslation>() {
                    @Override
                    public void onResponse(Call<YouDaoTranslation> call, retrofit2.Response<YouDaoTranslation> response) {
                        Log.e(TAG, "onResponse: " + response.body().show());
                    }

                    @Override
                    public void onFailure(Call<YouDaoTranslation> call, Throwable t) {

                    }
                });
        //onResponse: time:1, content:from=Happy New Year, to=新年快乐

    }

    interface BlueService {
        @GET("book/search")
        Call<ResponseBody> getSearchBooks(@Query("q")String name,
                                          @Query("tag")String tag,
                                          @Query("start")int start,
                                          @Query("count")int count);
    }


    private void testRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.douban.com/v2/")
                .build();

        BlueService service = retrofit.create(BlueService.class);

        Call<ResponseBody> call = service.getSearchBooks("长安十二时辰", "", 0, 3);

        //sync request
//        new Thread(() -> {
//            try {
//                Log.e(TAG, "testRetrofit: " + call.execute().body().string());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }, "retrofit").start();
        //testRetrofit: {"count":3,"start":0,"total":30,"books":[{"rating":{"max":10,"numRaters":6027,"average":"8.0","min":0},
        // "subtitle":"","author":["马伯庸"],"pubdate":"2017-1-1","tags":[{"count":1682,"name":"马伯庸","title":"马伯庸"},
        // {"count":1016,"name":"小说","title":"小说"},{"count":770,"name":"历史","title":"历史"},
        // {"count":741,"name":"唐朝","title":"唐朝"},{"count":551,"name":"长安十二时辰","title":"长安十二时辰"},
        // {"count":509,"name":"鬼才之思","title":"鬼才之思"},{"count":504,"name":"悬疑","title":"悬疑"},
        // {"count":419,"name":"架空小说","title":"架空小说"}],"origin_title":"","image":"https://img3.doubanio.com\/mpic\/s29247343.jpg",
        // "binding":"平装","translator":[],"catalog":"上册\n001\n第一章 巳正\n无数黑骑在远处来回驰骋。
        // 远处长河之上，一轮浑圆的血色落日；孤城城中，狼烟正直直刺向昏黄的天空。\n〈10 点〉\n02........

        //async request
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    Log.e(TAG, "onResponse: " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.toString());
            }
        });
        // same as above sync request


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
