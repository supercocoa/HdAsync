# HdAsync

**一个基于handler的android异步化lib**


`android`中自带了`HandlerThread/Handler`套件，使得在UI线程和后台线程等不通线程间异步操作时可以轻松通信/回调。但面对链式或层次式的异步调用时，往往会直接回调套回调，或者可能会把本来"连续"的逻辑拆的很远。同时，由于回调往往通过非静态内部类实现，在android中很容易造成Activity等资源的泄露。

`HdAsync`是一个基于handler的android异步化lib，在handler的基础上做了如下简单的封装：
* 参考了异步操作中常见的`promise-future`模式，可以轻松的组合异步链式/层次式的调用。在代码上，拉近异步操作间的"距离"，虽然达不到协程的效果，但也可以使逻辑尽可能的"紧凑"。
* 不直接限制在哪个线程执行，通过每一步`绑定`不同的`Looper`，使得可以多个不通线程间完成组合式的任务。
* 执行中的每一步都可以选择是否进行下一步，在同时使用别的异步调用时，可以`挂起`异步链，待回调回来时，再`恢复`异步链的执行。
*  可以通过`传递/返回`HdAsync，将自己的异步操作交给别的调用者来组合或调用。
*  通过`弱引用`与`静态内部类`以及`生命周期`的控制，最大化的降低内存泄露的可能。

## For example

轻松异步初始化Activity
``` Java
public class SampleActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {

    ...

    hdAsync = new HdAsync<SampleActivity>(this);

    hdAsync.ready().both(new HdAsyncAction(Looper.getMainLooper()) {
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

### 异步链
``` Java
hdAsync = new HdAsync<SampleActivity>(this);

hdAsync.both(new HdAsyncAction(Looper.getMainLooper()) { //并发调用
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
}).then(new HdAsyncAction(backgroundLooper) { //链式调用
  @Override
  public HdAsyncResult call(final HdAsyncArgs args) {
    // do something
    return args.doNext(false);
  }
}).delay(new HdAsyncAction(Looper.getMainLooper()) { //延时调用
  @Override
  public HdAsyncResult call(HdAsyncArgs args) {
    // do something
    return args.doNext(false);
  }
}, 200);


hdAsync.call(); //真正开始调用

```

### 内存问题
#### 短时操作

对于短时操作，为了便于编写，可以简单的借助组件本身的生命周期或在必要的时候来销毁异步链，从而避免内存泄露
``` Java
public class SampleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      // 声明为成员变量 为了在call的过程中，避免hdAsync被回收导致调用链失败
      hdAsync = new HdAsync<SampleActivity>(this);
    }

    public void foo() {
      hdAsync.then(new HdAsyncAction(backgroundLooper) { //链式调用
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
#### 长时操作

对于长时操作，由于java中`匿名内部类`会持有`外部类`的引用，所以在线程执行该操作时，可能会泄露其外部类。这里推荐的做法是实现一个`static`的HdAsync，将异步操作的实现挪到static class的ready中，并通过 `getHost`获得外部类引用，其他不则需要修改什么，就可以`完全避免`对外部类的`泄露`。

```  Java
public class SampleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      // 声明为成员变量 为了在call的过程中，避免hdAsync被回收导致调用链失败
      hdAsync = new StaticHdAsync<SampleActivity>(this);
    }

    public void foo() {
      hdAsync.then(new HdAsyncAction(backgroundLooper) { //链式调用
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
