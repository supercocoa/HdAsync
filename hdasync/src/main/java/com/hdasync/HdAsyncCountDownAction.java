package com.hdasync;

import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scott on 15/5/2.
 */
public abstract class HdAsyncCountDownAction extends BaseAction {

    private AtomicInteger countDownNum;

    public HdAsyncCountDownAction(Looper looper) {
        super(looper);
    }

    public HdAsyncCountDownAction(ExecutorService pool) {
        super(pool);
    }


    @Override
    public abstract HdAsyncCountDownResult call(Object args);

    public void setCountDownNum(AtomicInteger countDownNum) {
        this.countDownNum = countDownNum;
    }

    public HdAsyncCountDownResult doNextByCountDown(boolean needNext, Object value) {

        if (!needNext || countDownNum == null) {
            return new HdAsyncCountDownResult(false, value);
        }

        if (countDownNum.decrementAndGet() == 0) {
            return new HdAsyncCountDownResult(true, value);
        } else {
            return new HdAsyncCountDownResult(false, value);
        }
    }

    public HdAsyncCountDownResult doNextByCountDown(boolean needNext) {
        return doNextByCountDown(needNext, null);
    }
}
