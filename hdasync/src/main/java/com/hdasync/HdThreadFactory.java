package com.hdasync;

import android.os.HandlerThread;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by scott on 15/5/21.
 */
public class HdThreadFactory {

    public static final String ActivityInitThread = "ActivityInit_HandlerThread";

    public static final String BackGroundThread = "BackGround_HandlerThread";

    public static final String DBThread = "DB_HandlerThread";

    public static final String IOThread = "IO_HandlerThread";

    public static final String NetThread = "Net_HandlerThread";

    private static Map<String, HandlerThread> handlerThreadMap = new HashMap<String, HandlerThread>();

    public static HandlerThread getThread(String type) {
        HandlerThread handlerThread = handlerThreadMap.get(type);
        if (null == handlerThread) {
            handlerThread = new HandlerThread(type, getPriority(type));
            handlerThread.start();
            handlerThreadMap.put(type, handlerThread);

        } else {
            if (!handlerThread.isAlive()) {
                handlerThread.start();
            }
        }
        return handlerThread;
    }

    public static HandlerThread getThread(String type, boolean isDaemon) {
        HandlerThread handlerThread = handlerThreadMap.get(type);
        if (null == handlerThread) {
            handlerThread = new HandlerThread(type, getPriority(type));
            handlerThread.setDaemon(isDaemon);
            handlerThread.start();
            handlerThreadMap.put(type, handlerThread);

        } else {
            if (!handlerThread.isAlive()) {
                handlerThread.start();
            }
        }
        return handlerThread;
    }

    public static Looper getLooper(String type) {
        return getThread(type).getLooper();
    }


    private static int getPriority(String type) {

        if (ActivityInitThread.equalsIgnoreCase(type)) {

            return android.os.Process.THREAD_PRIORITY_DEFAULT;

        } else if (BackGroundThread.equalsIgnoreCase(type)) {

            return android.os.Process.THREAD_PRIORITY_BACKGROUND;

        } else if (DBThread.equalsIgnoreCase(type)) {

            return android.os.Process.THREAD_PRIORITY_BACKGROUND;

        } else if (IOThread.equalsIgnoreCase(type)) {

            return android.os.Process.THREAD_PRIORITY_BACKGROUND;

        } else if (NetThread.equalsIgnoreCase(type)) {

            return android.os.Process.THREAD_PRIORITY_BACKGROUND;

        } else {
            return android.os.Process.THREAD_PRIORITY_BACKGROUND;
        }
    }
}
