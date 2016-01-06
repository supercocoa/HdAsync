package com.hdasync;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scott on 15/3/26.
 */
public class AsyncActionGroup {

    public class ActionArray {
        BaseAction[] array;
    }

    private LinkedList<ActionArray> actionList;
    private AtomicInteger actionCount;

    public AsyncActionGroup() {
        this.actionList = new LinkedList<ActionArray>();
        this.actionCount = new AtomicInteger(0);
    }


    public ActionArray poll() {
        if (!actionList.isEmpty()) {
            return actionList.poll();
        }
        return null;
    }


    public void clear() {
        if (actionList != null) {
            actionList.clear();
        }
    }

    public boolean allActionFinish() {
        boolean ret = false;
        if (actionCount.get() <= 0) {
            ret = true;
        }
        return ret;
    }

    public void finishOneAction() {
        actionCount.decrementAndGet();
    }

    protected void then(final AsyncAction action) {
        actionCount.incrementAndGet();

        ActionArray actionArray = new ActionArray();
        actionArray.array = new AsyncAction[1];
        actionArray.array[0] = action;

        actionList.add(actionArray);
    }

    protected void delay(final AsyncAction action, long delay) {
        actionCount.incrementAndGet();
        action.delay = delay;

        ActionArray actionArray = new ActionArray();
        actionArray.array = new AsyncAction[1];
        actionArray.array[0] = action;

        actionList.add(actionArray);
    }

    protected void both(AsyncAction... actions) {
        ActionArray actionArray = new ActionArray();
        actionArray.array = actions;

        actionCount.addAndGet(actions.length);
        actionList.add(actionArray);
    }

    protected void both(AsyncCountDownAction... actions) {
        ActionArray actionArray = new ActionArray();
        actionArray.array = actions;

        actionCount.addAndGet(actions.length);
        actionList.add(actionArray);
    }

    protected void append(AsyncActionGroup group) {
        if (group != null) {
            actionCount.addAndGet(group.actionCount.get());
            actionList.addAll(group.actionList);
        }
    }


}
