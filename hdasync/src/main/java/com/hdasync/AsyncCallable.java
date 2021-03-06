package com.hdasync;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scott on 16/1/6.
 */
public class AsyncCallable {
    public static final String TAG = "AsyncCallable";

    private AsyncActionGroup actionGroup;
    private Object host;

    private boolean isCalling = false;
    private boolean isYield = false;
    private boolean isDone = false;

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    protected AsyncCallable(Object host) {
        this.host = host;
        actionGroup = new AsyncActionGroup();
    }


    public synchronized AsyncCallable call() {
        isYield = false;
        if (!isCalling) {
            isCalling = true;
            executeActionWithoutLock(null, true);
        }

        return this;
    }

    public synchronized AsyncCallable call(Object args) {
        isYield = false;
        if (!isCalling) {
            isCalling = true;
            executeActionWithoutLock(args, true);
        }
        return this;
    }

    public AsyncCallable resume() {
        call();
        return this;
    }

    public AsyncCallable resume(Object args) {
        call(args);
        return this;
    }

    public synchronized void yield() {
        isYield = true;
        isCalling = false;
    }

    public boolean isDone() {
        return isDone;
    }

    public synchronized void destroy() {
        destroyWithoutLock();
    }

    public void destroyWithoutLock() {
        if (actionGroup != null) {
            actionGroup.clear();
        }
    }

    public synchronized AsyncCallable then(final AsyncAction action) {
        if (action != null) {
            action.setHost(host);
            actionGroup.then(action);
        }

        return this;
    }

    public synchronized AsyncCallable delay(final AsyncAction action, long delay) {
        if (action != null) {
            action.setHost(host);
            actionGroup.delay(action, delay);
        }
        return this;
    }

    public synchronized AsyncCallable both(AsyncAction... actions) {
        if (actions != null) {
            for (AsyncAction action : actions) {
                action.setHost(host);
            }
            actionGroup.both(actions);
        }
        return this;
    }

    public synchronized AsyncCallable both(int countDownNum, AsyncCountDownAction... actions) {
        if (actions != null) {
            AtomicInteger atomicCountDownNum = new AtomicInteger(countDownNum);
            for (AsyncCountDownAction action : actions) {
                action.setHost(host);
                action.setCountDownNum(atomicCountDownNum);
            }
        }
        actionGroup.both(actions);
        return this;
    }

    public synchronized AsyncCallable append(AsyncCallable other) {
        if (other != null) {
            actionGroup.append(other.actionGroup);
        }
        return this;
    }


    static class Data {
        BaseAction action;
        Object args;
    }

    private synchronized void executeAction(Object args, boolean needNext) {
        executeActionWithoutLock(args, needNext);
    }

    private void executeActionWithoutLock(final Object args, boolean needNext) {
        if (actionGroup != null && !actionGroup.allActionFinish()) {

            if (!needNext || isYield) {
                return;
            }

            AsyncActionGroup.ActionArray actionArray = actionGroup.poll();

            if (actionArray == null) {
                return;
            }

            BaseAction[] actions = actionArray.array;

            for (final BaseAction action : actions) {

                if (action.looper == null && action.pool == null) {
                    continue;
                }

                action.setCallable(this); // setHdAsync in runtime

                final Data data = new Data();
                data.action = action;
                data.args = args;

                if (action.looper != null) {
                    executeByLooper(data);
                } else if (action.pool != null) {
                    executeByPool(data);
                }
            }

        } else {
            isCalling = false;
            isDone = true;
            destroyWithoutLock();
        }
    }

    private void executeByLooper(Data data) {
        Handler handler = new Handler(data.action.looper,
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        if (!(msg.obj instanceof Data)) {
                            return false;
                        }

                        Data data = (Data) msg.obj;
                        onActionExecute(data);
                        return false;
                    }
                });

        Message msg = Message.obtain();

        msg.obj = data;
        handler.sendMessageDelayed(msg, data.action.delay);
    }

    private void executeByPool(final Data data) {
        if (data.action.delay == 0) {
            submitToPool(data);
        } else {
            scheduleToPool(data);
        }
    }

    private void submitToPool(final Data data) {
        data.action.pool.submit(new Runnable() {
            @Override
            public void run() {
                onActionExecute(data);
            }
        });
    }

    private void scheduleToPool(final Data data) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                submitToPool(data);
            }
        }, data.action.delay, TimeUnit.MILLISECONDS);
    }

    private void onActionExecute(Data data) {
        BaseResult result = data.action.call(data.args);
        actionGroup.finishOneAction();
        if (result != null) {
            executeAction(result.value, result.needNext);
        }
    }

}
