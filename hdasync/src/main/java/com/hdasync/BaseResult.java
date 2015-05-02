package com.hdasync;

/**
 * Created by scott on 15/5/2.
 */
class BaseResult {
    protected HdAsyncArgs args;
    protected boolean needNext = false;

    protected BaseResult(boolean needNext, HdAsyncArgs args) {
        this.needNext = needNext;
        this.args = args;
    }
}
