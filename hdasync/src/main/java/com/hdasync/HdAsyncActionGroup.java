package com.hdasync;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by scott on 15/3/26.
 */
public class HdAsyncActionGroup {

    private LinkedList<HdAsyncAction[]> actionList;
    private AtomicInteger actionCount;

    public HdAsyncActionGroup() {
        this.actionList = new LinkedList<HdAsyncAction[]>();
        this.actionCount = new AtomicInteger(0);
    }


    public HdAsyncAction[] poll() {
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

    protected void then(final HdAsyncAction action) {
        actionCount.incrementAndGet();
        HdAsyncAction[] actions = new HdAsyncAction[1];
        actions[0] = action;
        actionList.add(actions);
    }

    protected void delay(final HdAsyncAction action, long delay) {
        actionCount.incrementAndGet();
        action.delay = delay;
        HdAsyncAction[] actions = new HdAsyncAction[1];
        actions[0] = action;
        actionList.add(actions);
    }

    protected void both(HdAsyncAction... actions) {
        actionCount.addAndGet(actions.length);
        actionList.add(actions);
    }

    protected void append(HdAsyncActionGroup group) {
        if (group != null) {
            actionCount.addAndGet(group.actionCount.get());
            actionList.addAll(group.actionList);
        }
    }


}
