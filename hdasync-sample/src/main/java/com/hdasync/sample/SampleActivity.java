package com.hdasync.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.hdasync.HdAsync;
import com.hdasync.HdAsyncAction;
import com.hdasync.HdAsyncCountDownAction;
import com.hdasync.HdAsyncCountDownResult;
import com.hdasync.HdAsyncResult;
import com.hdasync.HdThreadFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by scott on 15/3/26.
 */
public class SampleActivity extends Activity {

    boolean isInitFinish = false;
    View container;
    TextView testBtn;

    volatile HdAsync hdAsync;

    static Looper backgroundLooper = HdThreadFactory.getLooper(HdThreadFactory.BackGroundThread);

    static ExecutorService backgroundPool = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hdAsync = HdAsync.with(this)
                .then(new HdAsyncAction(getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        beforeInitAtMainThread(savedInstanceState);
                        return doNext(true);
                    }
                })
                .both(2, new HdAsyncCountDownAction(getMainLooper()) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        initAtMainThread();
                        return doNextByCountDown(true);
                    }
                }, new HdAsyncCountDownAction(backgroundLooper) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        initAtBackgroundThread();
                        return doNextByCountDown(true);
                    }
                })
                .then(new HdAsyncAction(getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
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

        hdAsync.then(new HdAsyncAction(Looper.getMainLooper()) {
            @Override
            public HdAsyncResult call(Object args) {
                resumeAtMainThread();
                return doNext(true);
            }
        }).call();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (hdAsync != null) {
            hdAsync.cancel();
            hdAsync.destroy();
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
        bindEvents();
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
        container = findViewById(R.id.container);
        container.setBackgroundColor(Color.parseColor("#00B0FF"));

        testBtn = (TextView) findViewById(R.id.test);
        testBtn.setBackgroundColor(Color.parseColor("#FF1744"));
    }

    private void bindEvents() {
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
    }

    private void initDatas() {
    }


    public void test() {
        hdAsync = createTestHdAsync(this).call();
    }


    public static HdAsync createTestHdAsync(SampleActivity host) {
        return HdAsync.with(host)
                .then(new HdAsyncAction(backgroundPool) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "1");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return doNext(true, true);
                    }
                })
                .then(new HdAsyncAction(Looper.getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
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
                .then(new HdAsyncAction(Looper.getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "3");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return doNext(true, false);
                    }
                })
                .then(new HdAsyncAction(Looper.getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
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
                .then(new HdAsyncAction(backgroundPool) {
                    @Override
                    public HdAsyncResult call(final Object args) {
                        Log.d(HdAsync.TAG, "5");

                        SampleActivity activity = (SampleActivity) getHost();
                        if (activity != null && getHdAsync() != null) {
                            activity.test2(new AsynTestClass(getHdAsync()));
                            getHdAsync().cancel();
                        }

                        return doNext(false);
                    }
                })
                .both(new HdAsyncAction(Looper.getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "6");

                        SampleActivity activity = (SampleActivity) getHost();
                        if (activity != null) {
                            activity.testBtn.setText("" + args);
                        }
                        return doNext(false);
                    }
                }, new HdAsyncAction(backgroundLooper) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "7");

                        return doNext(true);
                    }
                })
                .delay(new HdAsyncAction(Looper.getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
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
        HdAsync hdAsync3 = test3.test(asyncTest, backgroundLooper);

        HdAsync.with(this)
                .then(new HdAsyncAction(backgroundLooper) {
                    @Override
                    public HdAsyncResult call(Object args) {
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
        WeakReference<HdAsync> weakReference;

        public AsynTestClass(HdAsync hdAsync) {
            this.weakReference = new WeakReference<HdAsync>(hdAsync);
        }

        @Override
        public void onSuccess() {
            Log.d(HdAsync.TAG, "AsynTestClass onSuccess1");

            HdAsync hdAsync = weakReference.get();
            if (hdAsync != null) {
                Log.d(HdAsync.TAG, "AsynTestClass onSuccess2");
                hdAsync.resume(10);
            }
        }
    }


}
