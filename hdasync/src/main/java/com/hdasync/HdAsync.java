package com.hdasync;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * Created by scott on 15/3/26.
 */
public class HdAsync {

    public static final String TAG = "HdAsync";

    private HdAsyncArgs args;
    private HdAsyncActionGroup actionGroup;

    private boolean isCanceled = false;
    private boolean isDone = false;

    public static HdAsync with(Object host) {
        return new HdAsync(host);
    }

    private HdAsync(Object host) {
        actionGroup = new HdAsyncActionGroup();
        args = new HdAsyncArgs();
        args.setHdAsync(this);
        args.setHost(host);
    }


    public HdAsync call() {
        isCanceled = false;
        executeAction(args, true);
        return this;
    }

    public HdAsync call(HdAsyncArgs args) {
        isCanceled = false;
        this.args = args;
        executeAction(args, true);
        return this;
    }

    public HdAsync resume() {
        call();
        return this;
    }

    public HdAsync resume(HdAsyncArgs args) {
        call(args);
        return this;
    }

    public void cancel() {
        isCanceled = true;
    }

    public boolean isDone() {
        return isDone;
    }

    public void destroy() {
        if (actionGroup != null) {
            actionGroup.clear();
        }
        actionGroup = null;
        args = null;
    }

    public HdAsync then(final HdAsyncAction action) {
        actionGroup.then(action);
        return this;
    }

    public HdAsync delay(final HdAsyncAction action, long delay) {
        actionGroup.delay(action, delay);
        return this;
    }

    public HdAsync both(HdAsyncAction... actions) {
        actionGroup.both(actions);
        return this;
    }

    public HdAsync append(HdAsync other) {
        if (other != null) {
            actionGroup.append(other.actionGroup);
        }
        return this;
    }


    static class Data {
        HdAsyncAction action;
        HdAsyncArgs args;
    }

    private void executeAction(HdAsyncArgs args, boolean needNext) {
        if (actionGroup != null && !actionGroup.allActionFinish()) {

            if (!needNext || isCanceled) {
                return;
            }

            HdAsyncAction[] actions = actionGroup.poll();

            for (HdAsyncAction action : actions) {

                if (action.looper == null) {
                    continue;
                }

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
            isDone = true;
            destroy();
            Log.d(TAG, "isDone");
        }
    }


    class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            if (!(message.obj instanceof Data)) {
                return false;
            }

            Data data = (Data) message.obj;

            HdAsyncAction action = data.action;
            HdAsyncArgs args = data.args;

            HdAsyncResult result = action.call(args);

            actionGroup.finishOneAction();

            if (result != null) {
                executeAction(result.args, result.needNext);
            }

            return false;
        }

    }

}
