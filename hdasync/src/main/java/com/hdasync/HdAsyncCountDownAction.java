package com.hdasync;

import android.os.Looper;

/**
 * Created by scott on 15/5/2.
 */
public abstract class HdAsyncCountDownAction extends BaseAction {


    public HdAsyncCountDownAction(Looper looper) {
        super(looper);
    }

    @Override
    public abstract HdAsyncCountDownResult call(HdAsyncArgs args);
}
