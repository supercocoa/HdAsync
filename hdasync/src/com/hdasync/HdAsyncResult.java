package com.hdasync;

/**
 * Created by scott on 15/4/12.
 */
public class HdAsyncResult {
    protected HdAsyncArgs args;
    protected boolean needNext = false;

    protected HdAsyncResult(boolean needNext, HdAsyncArgs args) {
        this.needNext = needNext;
        this.args = args;
    }
}
