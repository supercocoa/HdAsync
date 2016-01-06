package com.hdasync;

import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scott on 15/5/2.
 */
public abstract class AsyncCountDownAction extends BaseAction {

    private AtomicInteger countDownNum;

    public AsyncCountDownAction(Looper looper) {
        super(looper);
    }

    public AsyncCountDownAction(ExecutorService pool) {
        super(pool);
    }


    @Override
    public abstract AsyncCountDownResult call(Object args);

    public void setCountDownNum(AtomicInteger countDownNum) {
        this.countDownNum = countDownNum;
    }

    public AsyncCountDownResult doNextByCountDown(boolean needNext, Object value) {

        if (!needNext || countDownNum == null) {
            return new AsyncCountDownResult(false, value);
        }

        if (countDownNum.decrementAndGet() == 0) {
            return new AsyncCountDownResult(true, value);
        } else {
            return new AsyncCountDownResult(false, value);
        }
    }

    public AsyncCountDownResult doNextByCountDown(boolean needNext) {
        return doNextByCountDown(needNext, null);
    }
}
