# HdAsync

**A android asynchronous communication library based on Handler**

`HandlerThread/Handler` emebed in Andorid framework makes UI thread and background thread communication easily.But when chained or hierarchical invoke happens,callbak logic is harder to handle.It turns out to be either callback inside callback or original  "continuous" logic being divided into a mess. Meanwhile because callback usually implemented as a innner static class which is likely leading to memory leak in Android enviroment.

`HdAsync` is not a standanlone lib,it depends on handler and encapsulate some functions below:
* Refrencing common used `promise-future` modle in async communication,HdAsync can combine chained or hierachical invoke as easily as you can image.On the coding level,this lib can shorten the 'logical distance' between asyncronous invokes.Though there is still a gap in achieving the effect of coroutine in some language, this libwill make  callback constucion as `compack` as possible.
* Instead of providing a thread to execute the code explicitly, choose a looper at every single step in chains, HdAync will do the rest for you and accomplish the whole combined task between different thread.
* User can choose whether connitue the task or not after a step finished.
* HdAsync can be returned or transfered to another caller to be combined or invoked.
* Through  mthods as  `weak reference`,`static innner class` and `lifecycle` management HdAsync minimize the posibility of memory leak.

## For example

Initialize a synchronous Activity

``` Java
public class SampleActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {

    ...

    hdAsync = new HdAsync<SampleActivity>(this);

    hdAsync.both(new HdAsyncAction(Looper.getMainLooper()) {
      @Override
      public HdAsyncResult call(HdAsyncArgs args) {
        initView();
        return args.doNext(false);
      }
    }, new HdAsyncAction(backgroundLooper) {
      @Override
      public HdAsyncResult call(HdAsyncArgs args) {
        initData(); //from db or srv
        return args.doNext(true);
      }
    }).then(new HdAsyncAction(Looper.getMainLooper()) {
      @Override
      public HdAsyncResult call(HdAsyncArgs args) {
        refreshUI();
        return args.doNext(false);
      }
    });

    hdAsync.call();
  }

  ...

}


```

## Quick Start

### Asynchronous chain
``` Java
hdAsync = new HdAsync<SampleActivity>(this);

hdAsync.both(new HdAsyncAction(Looper.getMainLooper()) {
  @Override
  public HdAsyncResult call(HdAsyncArgs args) {
    // do something
    return args.doNext(false);
  }
}, new HdAsyncAction(backgroundLooper) {
  @Override
  public HdAsyncResult call(HdAsyncArgs args) {
    // do something
    return args.doNext(true);
  }
}).then(new HdAsyncAction(backgroundLooper) {
  @Override
  public HdAsyncResult call(final HdAsyncArgs args) {
    // do something
    return args.doNext(false);
  }
}).delay(new HdAsyncAction(Looper.getMainLooper()) {
  @Override
  public HdAsyncResult call(HdAsyncArgs args) {
    // do something
    return args.doNext(false);
  }
}, 200);


hdAsync.call();

```

### Memory
#### Short term call

``` Java
public class SampleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      hdAsync = new HdAsync<SampleActivity>(this);
    }

    public void foo() {
      hdAsync.then(new HdAsyncAction(backgroundLooper) {
        @Override
        public HdAsyncResult call(final HdAsyncArgs args) {
            // do something
          return args.doNext(false);
        }
      });

      hdAsync.call();
    }


    @Override
    protected void onDestroy() {
      if (hdAsync != null) {
        hdAsync.destory();
        hdAsync = null;
      }
    }
}

```
#### Long term call


```  Java
public class SampleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      hdAsync = new StaticHdAsync<SampleActivity>(this);
    }

    public void foo() {
      hdAsync.then(new HdAsyncAction(backgroundLooper) {
        @Override
        public HdAsyncResult call(final HdAsyncArgs args) {
          // do something
          return args.doNext(false);
        }
      });

      hdAsync.call();
    }


    @Override
    protected void onDestroy() {

      if (hdAsync != null) {
        hdAsync.destory();
        hdAsync = null;
      }
    }

    static class StaticHdAsync extends HdAsync<SampleActivity> {

      public StaticHdAsync(SampleActivity host) {
        super(host);
      }

      @Override
      public void ready() {
        super.ready();
        then(new HdAsyncAction(backgroundLooper) {
          @Override
          public HdAsyncResult call(HdAsyncArgs args) {

            SampleActivity activity = getHost();
            if (activity != null) {
               //do something
            }
            args.setValue(true);
            return args.doNext(true);
          }
        });
      }
    }
}

```

## Chinese Versions
* [中文](README_zh.md)
