package com.hdasync;

import android.os.Looper;

import java.util.concurrent.ExecutorService;

/**
 * Created by scott on 15/3/26.
 */
public abstract class HdAsyncAction extends BaseAction {


    public HdAsyncAction(Looper looper) {
        super(looper);
    }

    public HdAsyncAction(ExecutorService pool) {
        super(pool);
    }

    @Override
    public abstract HdAsyncResult call(Object args);

    public HdAsyncResult doNext(boolean needNext, Object value) {
        return new HdAsyncResult(needNext, value);
    }

    public HdAsyncResult doNext(boolean needNext) {
        return doNext(needNext, null);
    }
}

