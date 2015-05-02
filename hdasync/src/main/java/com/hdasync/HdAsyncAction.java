package com.hdasync;

import android.os.Looper;

/**
 * Created by scott on 15/3/26.
 */
public abstract class HdAsyncAction extends BaseAction {

    public HdAsyncAction(Looper looper) {
        super(looper);
    }

    @Override
    public abstract HdAsyncResult call(HdAsyncArgs args);
}

