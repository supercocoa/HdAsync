package com.hdasync.sample;

import android.os.Looper;
import android.util.Log;
import com.hdasync.HdAsync;
import com.hdasync.HdAsyncAction;
import com.hdasync.HdAsyncArgs;
import com.hdasync.HdAsyncResult;

/**
 * Created by scott on 15/4/15.
 */
public class Test3 {

    public static final String TAG = "Test3";

    public HdAsync test(final IAsyncTest asyncTest, Looper backgroundLooper) {
        return Test3.createHdAsaync(this, asyncTest, backgroundLooper);
    }

    public static HdAsync createHdAsaync(Test3 host, final IAsyncTest asyncTest, Looper backgroundLooper) {
        return HdAsync.with(host)
                .then(new HdAsyncAction(backgroundLooper) {
                    @Override
                    public HdAsyncResult call(HdAsyncArgs args) {
                        Log.d(HdAsync.TAG, "test3 start");

                        if (args.getHost() != null) {
                            Log.d(HdAsync.TAG, ((Test3) args.getHost()).TAG);
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        asyncTest.onSuccess();
                        return args.doNext(true);
                    }
                });

    }

    static class HdAsyncHolder {
        public static HdAsync create(Test3 host, final IAsyncTest asyncTest, Looper backgroundLooper) {
            return HdAsync.with(host)
                    .then(new HdAsyncAction(backgroundLooper) {
                        @Override
                        public HdAsyncResult call(HdAsyncArgs args) {
                            Log.d(HdAsync.TAG, "test3 start");

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            asyncTest.onSuccess();
                            return args.doNext(true);
                        }
                    });

        }
    }

}
