package com.hdasync;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * Created by scott on 15/3/26.
 */
public class HdAsync<T> {

    public static final String TAG = "HdAsync";

    private HdAsyncArgs args;
    private HdAsyncActionGroup actionGroup;

    private boolean isReady = false;
    private boolean isCanceled = false;
    private boolean isDone = false;

    protected WeakReference<T> weakHost;

    public HdAsync(T host) {
        weakHost = new WeakReference<T>(host);
        actionGroup = new HdAsyncActionGroup();
        args = new HdAsyncArgs();
    }

    protected void ready() {
        isReady = true;
    }

    public T getHost() {
        if (weakHost != null) {
            return weakHost.get();
        }
        return null;
    }

    public void call() {
        if (!isReady) {
            ready();
        }
        isCanceled = false;
        executeAction(this, args, true);
    }

    public void call(HdAsyncArgs args) {
        if (!isReady) {
            ready();
        }
        isCanceled = false;
        this.args = args;
        executeAction(this, args, true);
    }

    public void resume() {
        call();
    }

    public void resume(HdAsyncArgs args) {
        call(args);
    }

    public void cancel() {
        isCanceled = true;
        isDone = true;
    }

    public boolean isDone() {
        return isDone;
    }

    public void destory() {
        cancel();
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
            if (!other.isReady) {
                other.ready();
            }
            actionGroup.append(other.actionGroup);
        }
        return this;
    }


    static class Data {
        WeakReference<HdAsync> weakAsync;
        WeakReference<HdAsyncAction> weakAction;
        HdAsyncArgs args;
    }

    private static void executeAction(HdAsync hdAsync, HdAsyncArgs args, boolean needNext) {
        if (hdAsync.actionGroup != null && !hdAsync.actionGroup.allActionFinish()) {

            if (!needNext) {
                return;
            }

            HdAsyncAction[] actions = hdAsync.actionGroup.poll();

            for (HdAsyncAction action : actions) {
                Handler handler = new Handler(action.looper, new HandlerCallback());
                Message msg = Message.obtain();
                Data data = new Data();
                data.weakAsync = new WeakReference<HdAsync>(hdAsync);
                data.weakAction = new WeakReference<HdAsyncAction>(action);
                data.args = args;
                msg.obj = data;
                if (action.delay == 0) {
                    handler.sendMessage(msg);
                } else {
                    handler.sendMessageDelayed(msg, action.delay);
                }

            }

        } else {
            hdAsync.isDone = true;
            Log.d(TAG, "isDone");
        }
    }


    static class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            if (!(message.obj instanceof Data)) {
                return false;
            }

            Data data = (Data) message.obj;

            WeakReference<HdAsync> weakAsync = data.weakAsync;
            WeakReference<HdAsyncAction> weakAction = data.weakAction;
            HdAsyncArgs args = data.args;

            if (weakAsync == null || weakAsync.get() == null || weakAction == null || weakAction.get() == null) {
                return false;
            }

            HdAsync hdAsync = weakAsync.get();

            HdAsyncAction action = weakAction.get();

            if (hdAsync.isCanceled) {
                return false;
            }

            HdAsyncResult result = action.call(args);

            hdAsync.actionGroup.finishOneAction();

            if (result != null) {
                executeAction(hdAsync, result.args, result.needNext);
            }

            return false;
        }

    }

}
