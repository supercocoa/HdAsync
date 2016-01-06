package com.hdasync;

/**
 * Created by scott on 15/3/26.
 */
public class HdAsync {

    public static final String TAG = "HdAsync";


    public static Callable with(Object host) {
        return new Callable(host);
    }


}
