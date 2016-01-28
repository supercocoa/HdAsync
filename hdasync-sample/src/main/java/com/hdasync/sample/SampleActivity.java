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
import com.hdasync.AsyncCallable;
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
    volatile AsyncCallable asyncCallable;


    static Looper backgroundLooper = HandlerThreadFactory.getLooper(HandlerThreadFactory.BackGroundThread);

    static ExecutorService backgroundPool = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        asyncCallable = HdAsync.with(this)
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

        asyncCallable.then(new AsyncAction(Looper.getMainLooper()) {
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
            asyncCallable.yield();
            asyncCallable.destroy();
        }
    }


    protected void beforeInitAtMainThread(Bundle savedInstanceState) {

    }

    protected void initAtMainThread() {
        initView();
    }


    protected void initAtBackgroundThread() {
        initDatas();
    }


    protected void afterInitAtMainThread() {
        //refresh
    }

    protected void activityResultAtMainThread(int requestCode, int resultCode, Intent data) {
        //onActivityResult
    }

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
        asyncCallable = createTestAsyncCallable(this).call();
    }

    @OnClick(R.id.container)
    public void onContainerClick() {
        Intent intent = new Intent();
        intent.setClass(SampleActivity.this, SecondActivity.class);
        startActivity(intent);
    }

    public static AsyncCallable createTestAsyncCallable(SampleActivity host) {
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
                            activity.testWithOtherAsyncFunc(new OtherAsyncCallback(getCallable()));
                            getCallable().yield();
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

    public void testWithOtherAsyncFunc(final IOtherAsyncCallback otherAsyncCallback) {
        OtherAsyncAction otherAsyncAction = new OtherAsyncAction();
        AsyncCallable otherAsyncCallbale = OtherAsyncAction.createAsyncCallable(otherAsyncAction, otherAsyncCallback, backgroundLooper);

        HdAsync.with(this)
                .then(new AsyncAction(backgroundLooper) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "testWithOtherAsyncFunc start");

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return doNext(true);
                    }
                })
                .append(otherAsyncCallbale)
                .call();
    }


    static class OtherAsyncCallback implements IOtherAsyncCallback {
        WeakReference<AsyncCallable> weakReference;

        public OtherAsyncCallback(AsyncCallable asyncCallable) {
            this.weakReference = new WeakReference<>(asyncCallable);
        }

        @Override
        public void onSuccess() {
            Log.d(HdAsync.TAG, "OtherAsyncCallback onSuccess1");


            AsyncCallable asyncCallable = weakReference.get();
            if (asyncCallable != null) {
                Log.d(HdAsync.TAG, "OtherAsyncCallback onSuccess2");
                asyncCallable.resume(10);
            }
        }
    }


}
