package com.hdasync;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scott on 15/3/26.
 */
public class HdAsyncArgs {

    private WeakReference<HdAsync> weakHdAsync;
    private WeakReference<Object> weakHost;
    private Object object;

    private AtomicInteger countDownNum;

    public void setHdAsync(HdAsync hdasync) {
        weakHdAsync = new WeakReference<HdAsync>(hdasync);
    }

    public HdAsync getHdAsync() {
        if (weakHdAsync != null) {
            return weakHdAsync.get();
        }
        return null;
    }

    public void setHost(Object host) {
        weakHost = new WeakReference<Object>(host);
    }

    public Object getHost() {
        if (weakHost != null) {
            return weakHost.get();
        }
        return null;
    }

    public void setValue(Object value) {
        object = value;
    }

    public Object getValue() {
        return object;
    }

    public void setCountDownNum(AtomicInteger countDownNum) {
        this.countDownNum = countDownNum;
    }

    public HdAsyncResult doNext(boolean needNext) {
        return new HdAsyncResult(needNext, this);
    }

    public HdAsyncCountDownResult doNextByCountDown(boolean needNext) {

        if (!needNext || countDownNum == null) {
            return new HdAsyncCountDownResult(false, this);
        }

        if (countDownNum.decrementAndGet() == 0) {
            return new HdAsyncCountDownResult(true, this);
        } else {
            return new HdAsyncCountDownResult(false, this);
        }

    }

}
