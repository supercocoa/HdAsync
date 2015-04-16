package com.hdasync;

import android.os.Looper;

/**
 * Created by scott on 15/3/26.
 */
public abstract class HdAsyncAction {

    protected Looper looper;

    protected long delay = 0;

    public HdAsyncAction(Looper looper) {
        this.looper = looper;
    }

    public abstract HdAsyncResult call(HdAsyncArgs args);
}

