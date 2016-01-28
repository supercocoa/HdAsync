package com.hdasync.sample;

import android.os.Looper;
import android.util.Log;
import com.hdasync.AsyncCallable;
import com.hdasync.HdAsync;
import com.hdasync.AsyncAction;
import com.hdasync.AsyncResult;

/**
 * Created by scott on 15/4/15.
 */
public class OtherAsyncAction {

    public static final String TAG = "OtherAsyncAction";

    public AsyncCallable test(final IOtherAsyncCallback otherAsyncCallback, Looper backgroundLooper) {
        return OtherAsyncAction.createAsyncCallable(this, otherAsyncCallback, backgroundLooper);
    }

    public static AsyncCallable createAsyncCallable(OtherAsyncAction host, final IOtherAsyncCallback otherAsyncCallback, Looper backgroundLooper) {
        return HdAsync.with(host)
                .then(new AsyncAction(backgroundLooper) {
                    @Override
                    public AsyncResult call(Object args) {
                        Log.d(HdAsync.TAG, "OtherAsyncAction start");

                        if (getHost() != null) {
                            Log.d(HdAsync.TAG, ((OtherAsyncAction) getHost()).TAG);
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        otherAsyncCallback.onSuccess();
                        return doNext(true);
                    }
                });

    }


}
