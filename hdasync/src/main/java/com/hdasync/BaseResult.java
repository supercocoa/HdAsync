package com.hdasync;

/**
 * Created by scott on 15/5/2.
 */
class BaseResult {
    protected Object value;
    protected boolean needNext = false;

    protected BaseResult(boolean needNext, Object value) {
        this.needNext = needNext;
        this.value = value;
    }
}
