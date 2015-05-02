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
import com.hdasync.HdAsyncCountDownAction;
import com.hdasync.HdAsyncCountDownResult;
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

    static Looper backgroundLooper;

    boolean hasWindowFocusChanged = false;


    static {
        HandlerThread handlerThread = new HandlerThread("back", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        backgroundLooper = handlerThread.getLooper();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = new View(this);
        v.setBackgroundColor(Color.WHITE);
        setContentView(v);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(HdAsync.TAG, "onWindowFocusChanged");

        if (hasWindowFocusChanged) {
            return;
        }

        hasWindowFocusChanged = true;

        HdAsync.with(this)
                .then(new HdAsyncAction(backgroundLooper) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "inflate");
                        View contentView = getLayoutInflater().inflate(R.layout.hdasync_sample_activity, null);
                        return doNext(true, contentView);
                    }
                })
                .both(1, new HdAsyncCountDownAction(getMainLooper()) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        Log.d(HdAsync.TAG, "initView");
                        initView((View) args);
                        bindEvents();

                        return doNextByCountDown(true);
                    }
                }, new HdAsyncCountDownAction(backgroundLooper) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        initDatas();

                        return doNextByCountDown(true);
                    }
                }, new HdAsyncCountDownAction(getMainLooper()) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        return doNextByCountDown(false);
                    }
                })
                .then(new HdAsyncAction(Looper.getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "refresh");
                        return doNext(false);
                    }
                })
                .call();
    }

    private void initView(View contentView) {
        setContentView(contentView);
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

    @Override
    protected void onDestroy() {

        if (hdAsync != null) {
            hdAsync.destroy();
            hdAsync = null;
        }

        Log.d(HdAsync.TAG, "onDestory");
        super.onDestroy();
    }


    public void test() {
        hdAsync = createTestHdAsync(this).call();
    }


    public static HdAsync createTestHdAsync(SampleActivity host) {
        return HdAsync.with(host)
                .then(new HdAsyncAction(backgroundLooper) {
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
                .then(new HdAsyncAction(backgroundLooper) {
                    @Override
                    public HdAsyncResult call(final Object args) {
                        Log.d(HdAsync.TAG, "5");

                        SampleActivity activity = (SampleActivity) getHost();
                        if (activity != null && getHdAsync() != null) {
                            activity.test2(new AsynTestClass(getHdAsync()));
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
                }, 200);

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
