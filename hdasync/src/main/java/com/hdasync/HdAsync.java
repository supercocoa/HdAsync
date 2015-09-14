package com.hdasync;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by scott on 15/3/26.
 */
public class HdAsync {

    public static final String TAG = "HdAsync";

    private HdAsyncActionGroup actionGroup;
    private Object host;

    private boolean isCalling = false;
    private boolean isCanceled = false;
    private boolean isDone = false;

    public static HdAsync with(Object host) {
        return new HdAsync(host);
    }

    private HdAsync(Object host) {
        this.host = host;
        actionGroup = new HdAsyncActionGroup();
    }


    public synchronized HdAsync call() {
        isCanceled = false;
        if (!isCalling) {
            isCalling = true;
            executeActionWithoutLock(null, true);
        }

        return this;
    }

    public synchronized HdAsync call(Object args) {
        isCanceled = false;
        if (!isCalling) {
            isCalling = true;
            executeActionWithoutLock(args, true);
        }
        return this;
    }

    public HdAsync resume() {
        call();
        return this;
    }

    public HdAsync resume(Object args) {
        call(args);
        return this;
    }

    public synchronized void cancel() {
        isCanceled = true;
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

    public synchronized HdAsync then(final HdAsyncAction action) {
        if (action != null) {
            action.setHost(host);
            actionGroup.then(action);
        }

        return this;
    }

    public synchronized HdAsync delay(final HdAsyncAction action, long delay) {
        if (action != null) {
            action.setHost(host);
            actionGroup.delay(action, delay);
        }
        return this;
    }

    public synchronized HdAsync both(HdAsyncAction... actions) {
        if (actions != null) {
            for (HdAsyncAction action : actions) {
                action.setHost(host);
            }
            actionGroup.both(actions);
        }
        return this;
    }

    public synchronized HdAsync both(int countDownNum, HdAsyncCountDownAction... actions) {
        if (actions != null) {
            AtomicInteger atomicCountDownNum = new AtomicInteger(countDownNum);
            for (HdAsyncCountDownAction action : actions) {
                action.setHost(host);
                action.setCountDownNum(atomicCountDownNum);
            }
        }
        actionGroup.both(actions);
        return this;
    }

    public synchronized HdAsync append(HdAsync other) {
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

    private void executeActionWithoutLock(Object args, boolean needNext) {
        if (actionGroup != null && !actionGroup.allActionFinish()) {

            if (!needNext || isCanceled) {
                return;
            }

            HdAsyncActionGroup.ActionArray actionArray = actionGroup.poll();

            if (actionArray == null) {
                return;
            }

            BaseAction[] actions = actionArray.array;

            for (BaseAction action : actions) {

                if (action.looper == null) {
                    continue;
                }

                action.setHdAsync(this); // setHdAsync in runtime

                Handler handler = new Handler(action.looper, new HandlerCallback());
                Message msg = Message.obtain();
                Data data = new Data();
                data.action = action;
                data.args = args;

                msg.obj = data;
                if (action.delay == 0) {
                    handler.sendMessage(msg);
                } else {
                    handler.sendMessageDelayed(msg, action.delay);
                }
            }

        } else {
            isCalling = false;
            isDone = true;
            destroyWithoutLock();
        }
    }


    class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            if (!(message.obj instanceof Data)) {
                return false;
            }

            Data data = (Data) message.obj;

            BaseAction action = data.action;
            Object args = data.args;

            BaseResult result = action.call(args);

            actionGroup.finishOneAction();

            if (result != null) {
                executeAction(result.value, result.needNext);
            }

            return false;
        }

    }

}
