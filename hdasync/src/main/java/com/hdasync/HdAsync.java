package com.hdasync;

/**
 * Created by scott on 15/3/26.
 */
public class HdAsync {

    public static final String TAG = "HdAsync";


    public static AsyncCallable with(Object host) {
        return new AsyncCallable(host);
    }

}
