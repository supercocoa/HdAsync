package com.hdasync.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hdasync.HdAsync;
import com.hdasync.HdAsyncAction;
import com.hdasync.HdAsyncArgs;
import com.hdasync.HdAsyncResult;
import com.hdasync.R;

import java.lang.ref.WeakReference;

/**
 * Created by scott on 15/3/26.
 */
public class SampleActivity extends Activity {

    View container;
    TextView testBtn;

    volatile HdAsync hdAsync;
    volatile HdAsync hdAsync2;

    static Looper backgroundLooper;


    static {
        HandlerThread handlerThread = new HandlerThread("back", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        backgroundLooper = handlerThread.getLooper();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        bindEvents();
        initDatas();
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
        hdAsync2 = new HdAsync<SampleActivity>(this);
    }

    @Override
    protected void onDestroy() {

        if (hdAsync != null) {
            hdAsync.destroy();
            hdAsync = null;
        }

        if (hdAsync2 != null) {
            hdAsync2.destroy();
            hdAsync2 = null;
        }

        Log.d(HdAsync.TAG, "onDestory");
        super.onDestroy();
    }


    public void test() {
        hdAsync = new StaticHdAsync(this);
        hdAsync.call();
    }


    static class StaticHdAsync extends HdAsync<SampleActivity> {

        public StaticHdAsync(SampleActivity host) {
            super(host);
        }

        @Override
        public void ready() {
            super.ready();
            then(new HdAsyncAction(backgroundLooper) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "1");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    args.setValue(true);
                    return args.doNext(true);
                }
            }).then(new HdAsyncAction(Looper.getMainLooper()) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "2");
                    if ((Boolean) args.getValue()) {
                        SampleActivity activity = getHost();
                        if (activity != null) {
                            activity.container.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                    args.setValue(false);
                    return args.doNext(true);
                }
            }).then(new HdAsyncAction(Looper.getMainLooper()) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "3");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return args.doNext(true);
                }
            }).then(new HdAsyncAction(Looper.getMainLooper()) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "4");
                    if (!(Boolean) args.getValue()) {
                        SampleActivity activity = getHost();
                        if (activity != null) {
                            activity.container.setBackgroundColor(Color.parseColor("#4CAF50"));
                        }
                    }
                    args.setValue(false);
                    return args.doNext(true);
                }
            }).then(new HdAsyncAction(backgroundLooper) {
                @Override
                public HdAsyncResult call(final HdAsyncArgs args) {
                    Log.d(TAG, "5");

                    SampleActivity activity = getHost();
                    if (activity != null) {
                        activity.test2(new AsynTestClass(StaticHdAsync.this, args));
                    }

                    return args.doNext(false);
                }
            }).both(new HdAsyncAction(Looper.getMainLooper()) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "6");

                    SampleActivity activity = getHost();
                    if (activity != null) {
                        activity.testBtn.setText("" + args.getValue());
                    }
                    return args.doNext(false);
                }
            }, new HdAsyncAction(backgroundLooper) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "7");
                    return args.doNext(true);
                }
            }).delay(new HdAsyncAction(Looper.getMainLooper()) {
                @Override
                public HdAsyncResult call(HdAsyncArgs args) {
                    Log.d(TAG, "8");

                    SampleActivity activity = getHost();
                    if (activity != null) {
                        activity.testBtn.setText("finish");
                    }
                    return args.doNext(false);
                }
            }, 200);

        }
    }

    public void test2(final IAsyncTest asyncTest) {

        hdAsync2.then(new HdAsyncAction(backgroundLooper) {
            @Override
            public HdAsyncResult call(HdAsyncArgs args) {
                Log.d(HdAsync.TAG, "test2 start");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return args.doNext(true);
            }
        });

        Test3 test3 = new Test3();
        HdAsync hdAsync3 = test3.test(asyncTest, backgroundLooper);
        hdAsync2.append(hdAsync3);
        hdAsync2.call();
    }


    static class AsynTestClass implements IAsyncTest {
        WeakReference<HdAsync> weakReference;
        HdAsyncArgs args;

        public AsynTestClass(HdAsync hdAsync, HdAsyncArgs args) {
            this.weakReference = new WeakReference<HdAsync>(hdAsync);
            this.args = args;
        }

        @Override
        public void onSuccess() {
            Log.d(HdAsync.TAG, "AsynTestClass onSuccess1");

            HdAsync hdAsync = weakReference.get();
            if (hdAsync != null) {
                Log.d(HdAsync.TAG, "AsynTestClass onSuccess2");
                args.setValue(10);
                hdAsync.resume(args);
            }
        }
    }


}
