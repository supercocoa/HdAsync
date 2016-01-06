package com.hdasync.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hdasync.AsyncAction;
import com.hdasync.AsyncCountDownAction;
import com.hdasync.AsyncCountDownResult;
import com.hdasync.AsyncResult;
import com.hdasync.Callable;
import com.hdasync.HandlerThreadFactory;
import com.hdasync.HdAsync;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by scott on 15/3/26.
 */
public class SampleActivity extends Activity {

    @Bind(R.id.container) View container;
    @Bind(R.id.test) TextView testBtn;

    boolean isInitFinish = false;

    volatile HdAsync hdAsync;
    volatile Callable callable;


    static Looper backgroundLooper = HandlerThreadFactory.getLooper(HandlerThreadFactory.BackGroundThread);

    static ExecutorService backgroundPool = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callable = HdAsync.with(this)
                .then(new AsyncAction(getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        beforeInitAtMainThread(savedInstanceState);
                        return doNext(true);
                    }
                })
                .both(2, new AsyncCountDownAction(getMainLooper()) {
                    @Override
                    public AsyncCountDownResult call(Object args) {
                        initAtMainThread();
                        return doNextByCountDown(true);
                    }
                }, new AsyncCountDownAction(backgroundLooper) {
                    @Override
                    public AsyncCountDownResult call(Object args) {
                        initAtBackgroundThread();
                        return doNextByCountDown(true);
                    }
                })
                .then(new AsyncAction(getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        afterInitAtMainThread();
                        isInitFinish = true;
                        return doNext(true);
                    }
                })
                .call();
    }

    @Override
    protected void onResume() {
        super.onResume();

        callable.then(new AsyncAction(Looper.getMainLooper()) {
            @Override
            public AsyncResult call(Object args) {
                resumeAtMainThread();
                return doNext(true);
            }
        }).call();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (hdAsync != null) {
            callable.cancel();
            callable.destroy();
        }
    }

    /**
     * step 0 初始化前  在主线程中  eg.可以pase intent等为初始化准备的工作
     *
     * @param savedInstanceState
     */
    protected void beforeInitAtMainThread(Bundle savedInstanceState) {

    }

    /**
     * step 1 初始化 在主线程中 eg.设置界面 绑定ui控件
     */
    protected void initAtMainThread() {
        initView();
    }

    /**
     * step 1 初始化 在后台线程中 eg.初始化service 从db读数据等
     */
    protected void initAtBackgroundThread() {
        initDatas();
    }

    /**
     * step 2 初始化后 在主线程中 eg.可以在初始化完后刷新界面
     */
    protected void afterInitAtMainThread() {
        //refresh
    }

    /**
     * step 2.5 从别的Activity回来 在主线程中
     */
    protected void activityResultAtMainThread(int requestCode, int resultCode, Intent data) {
        //onActivityResult
    }

    /**
     * step 3 resume 在主线程中
     */
    protected void resumeAtMainThread() {

    }


    private void initView() {
        setContentView(R.layout.hdasync_sample_activity);
        ButterKnife.bind(this);

        container.setBackgroundColor(Color.parseColor("#00B0FF"));
        testBtn.setBackgroundColor(Color.parseColor("#FF1744"));
    }


    private void initDatas() {
    }

    @OnClick(R.id.test)
    public void test() {
        callable = createTestHdAsync(this).call();
    }

    @OnClick(R.id.container)
    public void onContainerClick() {
        Intent intent = new Intent();
        intent.setClass(SampleActivity.this, SecondActivity.class);
        startActivity(intent);
    }

    public static Callable createTestHdAsync(SampleActivity host) {
        return HdAsync.with(host)
                .then(new AsyncAction(backgroundPool) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "1");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return doNext(true, true);
                    }
                })
                .then(new AsyncAction(Looper.getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "2");
                        if ((Boolean) args) {
                            SampleActivity activity = (SampleActivity) getHost();
                            if (activity != null) {
                                activity.container.setBackgroundColor(Color.TRANSPARENT);
                            }
                        }
                        return doNext(true, false);
                    }
                })
                .then(new AsyncAction(Looper.getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "3");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return doNext(true, false);
                    }
                })
                .then(new AsyncAction(Looper.getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "4");
                        if (!(Boolean) args) {
                            SampleActivity activity = (SampleActivity) getHost();
                            if (activity != null) {
                                activity.container.setBackgroundColor(Color.parseColor("#4CAF50"));
                            }
                        }
                        return doNext(true);
                    }
                })
                .then(new AsyncAction(backgroundPool) {
                    @Override
                    public AsyncResult call(final Object args) {
                        Log.d(HdAsync.TAG, "5");

                        SampleActivity activity = (SampleActivity) getHost();
                        if (activity != null && getCallable() != null) {
                            activity.test2(new AsynTestClass(getCallable()));
                            getCallable().cancel();
                        }

                        return doNext(false);
                    }
                })
                .both(new AsyncAction(Looper.getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "6");

                        SampleActivity activity = (SampleActivity) getHost();
                        if (activity != null) {
                            activity.testBtn.setText("" + args);
                        }
                        return doNext(false);
                    }
                }, new AsyncAction(backgroundLooper) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "7");

                        return doNext(true);
                    }
                })
                .delay(new AsyncAction(Looper.getMainLooper()) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "8");

                        SampleActivity activity = (SampleActivity) getHost();
                        if (activity != null) {
                            activity.testBtn.setText("finish");
                        }
                        return doNext(false);
                    }
                }, 1200);

    }

    public void test2(final IAsyncTest asyncTest) {
        Test3 test3 = new Test3();
        Callable hdAsync3 = test3.test(asyncTest, backgroundLooper);

        HdAsync.with(this)
                .then(new AsyncAction(backgroundLooper) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "test2 start");

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return doNext(true);
                    }
                })
                .append(hdAsync3)
                .call();
    }


    static class AsynTestClass implements IAsyncTest {
        WeakReference<Callable> weakReference;

        public AsynTestClass(Callable callable) {
            this.weakReference = new WeakReference<Callable>(callable);
        }

        @Override
        public void onSuccess() {
            Log.d(HdAsync.TAG, "AsynTestClass onSuccess1");

//            HdAsync hdAsync = weakReference.get();
//            if (hdAsync != null) {
//                Log.d(HdAsync.TAG, "AsynTestClass onSuccess2");
//                hdAsync.resume(10);
//            }
            Callable callable = weakReference.get();
            if (callable != null) {
                Log.d(HdAsync.TAG, "AsynTestClass onSuccess2");
                callable.resume(10);
            }
        }
    }


}
