# 重构之Android网络层
***网络层定义：在App中负责发送请求到服务器，获取数据返回并解析成数据对象的模块***

## 现状及思考

* 定义一个网络请求

```java 
@POST("supplier/register/")
Call<ResponseBody> register(@Body BodyRegisterV1 register);

@POST("supplier/login/")
Call<ResponseBody> login(@Body BodyLoginV1 login);

```
思考：为什么接口的声明都是ResponseBody？

理想情况：直接返回注册/登录 后写上相应的类型，比如这样写：

```java
@POST("supplier/register/")
DadaCall<ShopInfo> register(@Body BodyRegisterV1 register);

```
* 发起单个网络请求：

```java
BodyRegisterV1 register = xxx;
clientV1.register(register).enqueue(new ShopCallback(this, true) {
            @Override
            protected void onOk(ResponseBody responseBody) {
                final ShopInfo registerInfo = responseBody.getContentAs(ShopInfo.class);   
            }
        });
        
public abstract class ShopCallback extends Dada2RestCallback {
    public ShopCallback(Activity activity) {
        super(activity);
    }
    protected void checkTokenExpired(String errorCode) {
        //在请求fail的时候检查token是否过期，过期的话启动登陆页
    }
}   
```
思考：

* ShopCallback是结果的回调，全局的检查登录状态写在这里似乎不合适
* `responseBody.getContentAs(ShopInfo.class)`在UI线程做数据解析有性能问题 

```java 
public abstract class Dada2RestCallback extends Retrofit2Callback<ResponseBody> {
    private ProgressOperation dialogProgress;
    public Dada2RestCallback(Activity activity) {
        super(activity);
    }

    @Override
    protected final void onNetSuccess(Response<ResponseBody> response) {
        //httpcode＝200，根据业务状态回调业务成功或失败，同时控制ProgressOperation显示相应进度状态
    }

    protected abstract void onOk(ResponseBody responseBody);
    protected void onFailed(ResponseBody responseBody) {
    	//显示失败信息
    }
    protected void onError(Retrofit2Error error) {
 		//显示异常信息
    }
}  
``` 
思考：Dada2RestCallback是结果的回调，但业务回调状态的控制以及全局的错误信息显示写在这似乎也不合理

```java
  public abstract class Retrofit2Callback<T> implements retrofit2.Callback<T> {
    private WeakReference<Activity> weakReference;
    public Retrofit2Callback(Activity activity) {
        weakReference = new WeakReference<>(activity);
    }

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        //在activity没有被销毁的状态下，检查http状态码，执行相应回调
    }
    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        //在activity没有被销毁的状态下，执行error回调
    }

    protected abstract void onNetSuccess(Response<T> response);
    protected abstract void onNetError(Retrofit2Error error);
}   
//
//
```
思考：

* Retrofit2Callback是结果的回调，但http回调状态的控制以及是否要执行回调的逻辑写在这似乎也不合理；
* 这个类耦合了activity，那么如果我希望我的网络请求跟fragment/view的生命周期绑定，就没法做了

正确的做法应该是请求的回调就应该是一个干净的接口，控制的细节应该要隐藏，要让大家使用起来，感觉这个框架就是专门为这个app打造的。这其实对应了java设计的开闭原则中的对修改关闭。不然的话，我直接new一个retrofit2.Callback的实现类，就把我们的设计给废了。

理想情况：

```java
clientV1.register(register).progress(progressOperation)
              	//也支持.fail和.error进行链式调用
                .ok(shopInfo -> {
                    //把shopInfo存起来，并更新UI
                }).enqueue(lifecycleOwner);


//或者干脆直接返回ApiResponse<ShopInfo>
clientV1.register(register).progress(progressOperation)
                .enqueue(lifecycleOwner, apiResponse -> {
                    if(apiResponse.isOk()){
                        //把shopInfo存起来，并更新UI
                    }else {
                        //提示错误信息
                    }
                });
```

```java
public interface LifecycleOwner {
    Lifecycle getLifecycle();
}
```

* 发起多个网络请求：

```
//构造register参数...
clientV1.register(register).enqueue(new ShopCallback(this, true) {
            @Override
            protected void onOk(ResponseBody responseBody) {
                final ShopInfo registerInfo = responseBody.getContentAs(ShopInfo.class);
                clientV1.shopDetailInfo(registerInfo.getUserId()).enqueue(new ShopCallback(getActivity()) {
                    @Override
                    protected void onOk(ResponseBody responseBody) {
                        ShopDetail detail = responseBody.getContentAs(ShopDetail.class);
                        //存储ShopDetail...
                    }
                });
            }

            @Override
            protected void onFailed(ResponseBody responseBody) {
                if (!dealWithErrorResult(responseBody))
                    super.onFailed(responseBody);
            }
        });
```
用错了吧。。。这不是现有设计的正确使用姿势。

* 正确的发起多个网络请求：

```
new HttpAsyTask<Void, Void>(activity, new WaitDialog(activity).getProgressDialog()) {
            @Override
            protected ResponseBody workInBackground(Void... params) throws IOException {
                if (isCancelled())
                    return null;

                // Step - 1
                Call<ResponseBody> call = clientV1.rechargeCheck(new BodyRechargeCheckV1(userId, couponId, payAmount, rechargeOrderId));
                Response<ResponseBody> response = call.execute();

                if (isCancelled())
                    return null;

                // Step - 2
                if (!response.isSuccessful() || !response.body().isOk())
                    return response.body();

                String rechargeToken = response.body().getContentAsObject().optString("rechargeToken");
                Call<ResponseBody> call2 = clientV1.payRecharge(new BodyPayRechargeV1(userId, rechargeToken, payWay));

                return call2.execute().body();
            }

            @Override
            public void onOk(ResponseBody responseBody) {
                //这个判断没必要吧，都走到onOk了，isCancelled肯定是false
                if (isCancelled())
                    return;
					//更新UI
                }
            }

            @Override
            public void onFailed(ResponseBody responseBody) {
                super.onFailed(responseBody);
                view.getPayParamFailed();
            }

            @Override
            protected void onError(@Nullable RetrofitError error) {
                super.onError(error);
                view.getPayParamFailed();
            }
        };
```
思考：

* 重复的`if (isCancelled())`判断
* 重复的`view.getPayParamFailed();`书写
* 判断请求业务不成功写法`!response.isSuccessful() || !response.body().isOk()`，既麻烦又很重复
* 回调与逻辑实现（`AsyncTask`）强耦合，思岗说我想换成rxjava来实现，那已经写好的代码怎么办

理想的情况是：

```java
Task<Void,JSONObject> tokenTask = ((lifeState, apiResponse) -> clientV1.rechargeCheck(new BodyRechargeCheckV1(userId, couponId, payAmount, rechargeOrderId)).execute());
Task<JSONObject,JSONObject> payTask = ((lifeState, apiResponse) -> clientV1.payRecharge(new BodyPayRechargeV1(userId, apiResponse.getContent().getString("rechargeToken"), payWay)).execute());

/** 简化一下：
Task<Void,JSONObject> tokenTask = ((lifeState, apiResponse) -> clientV1.method1().execute());
Task<JSONObject,JSONObject> payTask = ((lifeState, apiResponse) -> clientV1.method2("来自上一步的参数").execute());
*/

/** java8不熟悉的看这里
Task<Void,JSONObject> tokenTask = new Task<Void, JSONObject>() {
            @Override
            public ApiResponse<JSONObject> doTask(@Nullable LifeState lifeState, @Nullable ApiResponse<Void> apiResponse) {
                return clientV1.method1().execute();
            }
        };
Task<JSONObject,JSONObject> payTask = new Task<JSONObject, JSONObject>() {
            @Override
            public ApiResponse<JSONObject> doTask(@Nullable LifeState lifeState, @Nullable ApiResponse<JSONObject> apiResponse) {
                return clientV1.method2("来自上一步的参数").execute();
            }
        };
*/

DadaCall<JSONObject> dadaCall=MergeDadaCall.task(tokenTask,payTask);

```
上面的代码应该写在Model层，下面的代码存在于Presenter层。

```java
dadaCall.enqueue(lifecycleOwner, apiResponse -> {
            if(apiResponse.isOk()){
                //apiResponse.getContent()取出数据更新UI
            }else{
                view.getPayParamFailed();
            }
        });

```


## 设计（Alpha版）探讨
核心类`DadaCall`

```java
/**
 * Created by tomkeyzhang on 3/1/18.
 * 为达达定制的http响应抽象
 */

public interface DadaCall<T> {
    /**
     * 设置http200 ok的回调
     */
    DadaCall<T> ok(@NonNull Observer<T> observer);

    /**
     * 设置http200 fail的回调
     */
    DadaCall<T> fail(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 设置error的回调
     */
    DadaCall<T> error(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 设置进度监听
     */
    DadaCall<T> progress(@NonNull ProgressOperation progressOperation);

    /**
     * 执行异步请求，绑定组件生命周期(获取全部状态结果)
     */
    void enqueue(@NonNull LifecycleOwner owner, @NonNull Observer<ApiResponse<T>> observer);

    /**
     * 执行异步请求，绑定组件生命周期（获取部分状态结果）
     */
    void enqueue(@NonNull LifecycleOwner owner);

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取部分状态结果)
     */
    void enqueue();

    /**
     * 执行异步请求，但不需要绑定组件生命周期(获取全部状态结果)
     */
    void enqueue(@NonNull Observer<ApiResponse<T>> observer);

    /**
     * 发起同步网络请求
     */
    ApiResponse<T> execute();

    /**
     * 取消请求
     * 1、对于单个http请求，取消时如果还没有开始执行，则不执行；如果在执行中，则执行结束不会回调
     * 2、对于多个连续http请求，除了1的特性外，取消后剩下的未开始执行请求也不会被执行
     */
    void cancel();

    interface Interceptor {

        /**
         * 返回true表示停止下一步执行
         */
        boolean preExecute();

        /**
         * 对于异步请求，返回true表示停止下一步执行
         */
        boolean onResponse(ApiResponse apiResponse);
    }

    Observer OBSERVER_NONE = (t) -> {
    };
}
```
生命周期扩展类LifeState

```java
/**
 * Created by tomkeyzhang on 11/1/18.
 * 用于获取组件生命状态
 */
public class LifeState {
    Lifecycle lifecycle;
    AtomicBoolean cancelled = new AtomicBoolean(false);

    public LifeState(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Lifecycle.State getCurrentState() {
        return lifecycle.getCurrentState();
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);
    }
}
```
任务抽象`Task<F, T>`，用于发起多个http请求

```java
/**
 * Created by tomkeyzhang on 11/1/18.
 * http请求任务抽象，执行一个任务，将对象F转换成T
 * F:from type
 * T:to type
 */

public interface Task<F, T> {
    ApiResponse<T> doTask(@Nullable LifeState lifeState, @Nullable ApiResponse<F> apiResponse);
}

```
`LifeState `包装类`ContainerLifeState `

```java
/**
 * Created by tomkeyzhang on 12/1/18.
 * 在ContainerState中包装一个LifeState。
 * 当我们在需要一个ContainerState，而手上只有一个LifeState对象时可以使用
 */

public class ContainerLifeState implements ContainerState {

    private LifeState lifeState;

    public ContainerLifeState(@NonNull LifeState lifeState) {
        this.lifeState = lifeState;
    }

    @Override
    public State state() {
        if (lifeState.isCancelled() || lifeState.getCurrentState() == DESTROYED)
            return State.DEAD;
        return State.READY;
    }
}

```
这个类的作用是什么？当我们在同步执行多个请求时，需要在第一个请求执行完通知给用户发个通知。
这时可以这样用：

```java
Task<JSONObject,JSONObject> payTask = ((lifeState, apiResponse) ->{ 
	handler.post(new SafeRunnable(runnable,new ContainerLifeState(lifeState)))
	clientV1.method2("来自上一步的参数").execute()
});

```

## 单元测试
### 单个请求实现类DefaultDadaCall的测试用例
* testSyn200Ok：验证同步方法http200+ok
* testSyn200Unknown：验证同步方法http200+未知内容
* testSyn200Fail：验证同步方法http200+fail
* testSyn404：验证同步方法http404
* testSyn502：验证同步方法http502
* testSynThrowable：验证同步方法抛异常，如SocketTimeoutException
* test200Ok：验证异步方法http200+ok
* test200Unknown：验证异步方法http200+未知内容
* test200Fail：验证异步方法http200+fail
* test404：验证异步方法http404
* test502：验证异步方法http502
* testThrowable：验证异步方法抛异常，如SocketTimeoutException

### 多个连续请求实现类MergeDadaCall的测试用例
* test404：验证异步方法http200+ok
* test200_intercept_return_true：验证拦截器是否正常拦截
* test_cancelled：验证请求主动取消是否正确
* test_doTask_return_null：验证`Task.doTask`返回异常值null时代码是否正常
* test200ok_with_jsonObject_jsonArray_customObject：验证Task结果转化是否正常
