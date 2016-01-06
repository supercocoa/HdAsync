package com.hdasync.sample;

import android.os.Looper;
import android.util.Log;
import com.hdasync.Callable;
import com.hdasync.HdAsync;
import com.hdasync.AsyncAction;
import com.hdasync.AsyncResult;

/**
 * Created by scott on 15/4/15.
 */
public class Test3 {

    public static final String TAG = "Test3";

    public Callable test(final IAsyncTest asyncTest, Looper backgroundLooper) {
        return Test3.createHdAsaync(this, asyncTest, backgroundLooper);
    }

    public static Callable createHdAsaync(Test3 host, final IAsyncTest asyncTest, Looper backgroundLooper) {
        return HdAsync.with(host)
                .then(new AsyncAction(backgroundLooper) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "test3 start");

                        if (getHost() != null) {
                            Log.d(HdAsync.TAG, ((Test3) getHost()).TAG);
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        asyncTest.onSuccess();
                        return doNext(true);
                    }
                });

    }

    static class HdAsyncHolder {
        public static Callable create(Test3 host, final IAsyncTest asyncTest, Looper backgroundLooper) {
            return HdAsync.with(host)
                    .then(new AsyncAction(backgroundLooper) {
                        @Override
                        public AsyncResult call(Object args) {
                            Log.d(HdAsync.TAG, "test3 start");

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            asyncTest.onSuccess();
                            return doNext(true);
                        }
                    });

        }
    }

}
