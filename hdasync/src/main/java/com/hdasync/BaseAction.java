package com.hdasync;

import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

/**
 * Created by scott on 15/5/2.
 */
abstract class BaseAction {
    protected Looper looper;

    protected ExecutorService pool;

    protected long delay = 0;

    private WeakReference<HdAsync> weakHdAsync;
    private WeakReference<Object> weakHost;

    public BaseAction(Looper looper) {
        this.looper = looper;
        this.pool = null;
    }

    public BaseAction(ExecutorService pool) {
        this.pool = pool;
        this.looper = null;
    }

    public abstract BaseResult call(Object args);


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

}
