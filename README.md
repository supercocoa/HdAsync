# HdAsync

**An android asynchronous operation library**

`HdAsync` is an android library to help asynchronous operation easily.

* support each step of asynchronous operation flow bind to handler thread looper or thread pool.
* support `then`, `both` and `delay`  operation.
* `append`  can add other async operation to the current flow.
* simple and clean


## For example

#### Initialize a synchronous Activity

``` Java
public class SampleActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HdAsync.with(this)
                .then(new HdAsyncAction(getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        beforeInitAtMainThread(savedInstanceState);
                        return doNext(true);
                    }
                })
                .both(2, new HdAsyncCountDownAction(getMainLooper()) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        initAtMainThread();
                        return doNextByCountDown(true);
                    }
                }, new HdAsyncCountDownAction(backgroundPool) {
                    @Override
                    public HdAsyncCountDownResult call(Object args) {
                        initAtBackgroundThread();
                        return doNextByCountDown(true);
                    }
                })
                .then(new HdAsyncAction(getMainLooper()) {
                    @Override
                    public HdAsyncResult call(Object args) {
                        afterInitAtMainThread();
                        isInitFinish = true;
                        return doNext(true);
                    }
                })
                .call();
    }
}    

```

####  Async get data from db and render
``` java
HdAsync asyncGetDataFromDb() {
    return HdAsync.with(this)
        .then(new HdAsyncAction(backgroundPool) {
                @Override
                public HdAsyncResult call(Object args) {
                    Data data = getDataFromDb();
                    return doNext(true, data);
                }
            });
}

void render() {
    HdAsync.with(this)
        .append(asyncGetDataFromDb())
        .then(new HdAsyncAction(Looper.getMainLooper()) {
                @Override
                public HdAsyncResult call(Object args) {
                    if ((Data) args) {
                        onRender(data);
                    }
                    return doNext(false);
                }
            })
        .call();
}
```
