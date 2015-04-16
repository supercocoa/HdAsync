package com.hdasync;

/**
 * Created by scott on 15/3/26.
 */
public class HdAsyncArgs {
    protected Object object;

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
