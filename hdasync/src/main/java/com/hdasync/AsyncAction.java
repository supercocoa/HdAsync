package com.hdasync;

import android.os.Looper;

import java.util.concurrent.ExecutorService;

/**
 * Created by scott on 15/3/26.
 */
public abstract class AsyncAction extends BaseAction {


    public AsyncAction(Looper looper) {
        super(looper);
    }

    public AsyncAction(ExecutorService pool) {
        super(pool);
    }

    @Override
    public abstract AsyncResult call(Object args);

    public AsyncResult doNext(boolean needNext, Object value) {
        return new AsyncResult(needNext, value);
    }

    public AsyncResult doNext(boolean needNext) {
        return doNext(needNext, null);
    }
}

