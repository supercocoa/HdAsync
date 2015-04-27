package com.hdasync;

import java.lang.ref.WeakReference;

/**
 * Created by scott on 15/3/26.
 */
public class HdAsyncArgs {

    protected WeakReference<HdAsync> weakHdAsync;
    protected WeakReference<Object> weakHost;
    protected Object object;

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

    public HdAsyncResult doNext(boolean needNext) {
        return new HdAsyncResult(needNext, this);
    }

}
