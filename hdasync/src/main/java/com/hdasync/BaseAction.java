package com.hdasync;

import android.os.Looper;

/**
 * Created by scott on 15/5/2.
 */
abstract class BaseAction {
    protected Looper looper;

    protected long delay = 0;

    public BaseAction(Looper looper) {
        this.looper = looper;
    }

    public abstract BaseResult call(HdAsyncArgs args);
}
