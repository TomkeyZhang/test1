# 测试入门之Mockito框架

## 为什么要使用Mock框架
上节熟悉并练习了Junit框架的使用后，我们已经可以写简单的单元测试代码来验证一个函数的返回结果是否正确。但是所面对的实际业务逻辑并没有那么简单，在写测试代码的时候咱们还可能碰到下面两个典型问题：

* 函数没有返回值怎么测
* 测试一个函数的时候有一堆依赖怎么处理

咱们看一个非常简单的登录功能，实现代码如下：

```java
public class LoginPresenter {
    RestClient restClient;

    public LoginPresenter(RestClient restClient) {
        this.restClient = restClient;
    }

    public void login(String name, String password) {
        if (name == null || name.length() >= 12 )
            throw new VerifyException("名称长度必须大于1且小于12！");
        
        restClient.login(name, password);
    }
}
```

咱们如何测试Presenter的登录方法呢？首先我们要知道我们需要测什么：

* name参数正确的时候应该执行`restClient.login(name, password)`方法
* name参数错误的时候应该抛出异常


根据咱们上节的知识，我们可以这样写出测试代码：

```java
public class LoginPresenterTest1 {
    RestClient restClient;
    LoginPresenter loginPresenter;

    @Before
    public void setup(){
        restClient=new RestClient() {
            @Override
            public void login(String name, String password) {
                System.out.println("login name=" + name + " password=" + password);
            }
        };
        loginPresenter=new LoginPresenter(restClient);
    }

    @Test
    public void testLoginSuccess(){
        loginPresenter.login("张三","123");
    }//查看是否是打印出login字符串
    
    @Test(expected = LoginException.class)
    public void testLogin_failed1(){
        loginPresenter.login(null,"123");
    }//用户名为null
    
    @Test(expected = LoginException.class)
    public void testLogin_failed2(){
        loginPresenter.login("张三张三张三张三张三张三","123");
    }//用户名长度等于12
}
```

这个代码可以正确运行，并且可以达到我们的测试目的，但是他有哪些问题？

* 我们需要肉眼去看是否正确打印出结果
* 如果RestClient接口有几十个方法，我们的代码将会非常难看

因此需要一个工具来帮助我们创建RestClient接口的代理实现类，用这个类我们可以很方便的进行断言方法是否被调用。实际上这就是Mock框架会帮我们做的事情，稍微抽象一点来说，就是创建一个类的虚假对象，在测试环境中，用来替换掉真实的对象，以达到两大目的：

* **验证这个对象的某些方法的调用情况，调用了多少次，参数是什么等等**
* **指定这个对象的某些方法的行为，返回特定的值，或者是执行特定的动作**

下面咱们的例子是基于Mockito框架，这个框架目前应该是Java界使用最广泛的一个mock框架，也是android官方单元测试示例使用的框架。使用Mockito改造后的代码（LoginPresenterTest2.java）：

```java
@Before
public void setup(){
	 //<== 变更1
    restClient= Mockito.mock(RestClient.class);
    loginPresenter=new LoginPresenter(restClient);
}

@Test
public void testLoginSuccess(){
    loginPresenter.login("张三","123");
    //<== 变更2
    Mockito.verify(restClient).login("张三","123");
}
```

## Mockito常见用法
### 1、验证方法调用
例如上面这行代码：`Mockito.verify(restClient).login("张三","123")`我们restClient对象的login方法以参数`"张三","123"`被调用了1次

* **如何验证方法被多次调用呢？**   
`Mockito.verify(restClient,Mockito.times(3)).login("张三","123");`  
另外还可以验证最多`atMost(count)`、最少`atLeast(count)`、从来没有`never()`等
* **方法参数的校验。** 有时候我们并不关心被调用方法的参数具体是什么，或者是我也不知道，我只关心这个方法得到调用了就行。这种情况下，Mockito提供了一系列的any方法，来表示任何的参数都行  `Mockito.verify(restClient).login(Mockito.anyString(),Mockito.anyString());`  
其中的`anyString()`表示任何一个字符串都可以（包括null），类似的还有`anyInt()`，`anyLong()`，`anyDouble()`等。其他的还有`anyObject()`，`any(clazz)`，`anyCollection()`，`anyCollectionOf(clazz)`，`anyListOf(clazz)`，`anyMapOf(keyClazz,valueClazz)`等等
* **验证方法的执行顺序。** 有时候我们需要严格要求一种方法在另一种方法之前执行，可以这样写：

```java
 List singleMock = mock(List.class);

 singleMock.add("第一个元素");
 singleMock.add("第二个元素");

 //创建一个InOrder验证器
 InOrder inOrder = inOrder(singleMock);

 //下面的代码可以确保“第一个元素”在“第二个元素”之前添加 
 inOrder.verify(singleMock).add("第一个元素");
 inOrder.verify(singleMock).add("第二个元素");

```
> 大部分时候我们会使用 import static org.mockito.Mockito.* 导入这个类的所有静态方法，这样就不用每次加上Mockito.前缀

### 2、指定mock对象方法的行为（官方称为打桩stubbing）
主要包括两方面：

* **指定方法返回特定值。** 还是用之前登录的例子，现在咱们增加一个需求，登录的时候在客户端直接让一些不合法的名称不可以登录，这时我们需要一个名称校验器NameVerifier

```java
public interface NameVerifier {
    boolean verify(String name);
}
```
> 它具体的实现可能是从本地数据库中去匹配名称是否合法

这时我们的login方法的代码可以这样写：

```java
public void login(String name, String password) {
        if (name == null || name.length() >= 12)
            throw new VerifyException("名称长度必须大于1且小于12！");
        if (nameVerifier.illegal(name))
            throw new VerifyException("名称不合法！");
            
        restClient.login(name, password);
}
```
测试代码可以这么写（LoginPresenterTest3.java）：

```java
@Before
public void setup(){
    restClient= Mockito.mock(RestClient.class);
    nameVerifier=Mockito.mock(NameVerifier.class);
    loginPresenter=new LoginPresenter2(restClient,nameVerifier);
    //<==注意这里
    Mockito.when(nameVerifier.illegal("张三")).thenReturn(false);
    Mockito.when(nameVerifier.illegal("法轮功")).thenReturn(true);
}

@Test
public void testLoginSuccess(){
    loginPresenter.login("张三","123");
    Mockito.verify(restClient).login("张三","123");
}

@Test(expected = VerifyException.class)
public void testLogin_failed3(){
    loginPresenter.login("法轮功","123");
}//敏感词

```

某些情况下，如果我们不需要根据方法的入参来决定返回值时，例如我们认为任何name都是合法的：  
`when(nameVerifier.illegal(anyString())).thenReturn(false);`

* **指定执行特定动作。** 咱们在原来的基础上优化一下登录功能，增加登录成功后更新一下UI（比如弹个Toast），于是按照MVP的设计思想，我们定义一个LoginView：

```java
public interface LoginView {
    void loginSuccess(String data);
}
```

这时我们的login方法的代码可以这样写：

```java
public void login(String name, String password) {
        if (name == null || name.length() >= 12)
            throw new VerifyException("名称长度必须大于1且小于12！");

        restClient.login(name, password, new NetworkCallback<String>() {
            @Override
            public void onSuccess(String data) {
                //保持用户信息等
                loginView.loginSuccess(data);
            }

            @Override
            public void onFailure(int code, Throwable cause) {
            }
        });
}
```

那么问题来了，在restClient并不是一个真实的对象的情况下，我们该如何验证`loginView.loginSuccess(data)`方法被调用呢？貌似这个`NetworkCallback`完全不受我们控制啊！好在Mockito也考虑到了这一点，下面看看我们的测试代码该怎么写：

```java
public void testLoginSuccess(){
        //<==注意这里
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //这里可以获得传给login的参数
                Object[] arguments = invocation.getArguments();

                //callback是第三个参数
                NetworkCallback callback = (NetworkCallback) arguments[2];

                callback.onSuccess("ok");
                return "";
            }
        }).when(restClient).login(anyString(),anyString(),any(NetworkCallback.class));
        //执行login()方法
        loginPresenter.login("张三","123");
        //断言回调了restClient.login()方法
        Mockito.verify(restClient).login(eq("张三"), eq("123"), any(NetworkCallback.class));
        
        //断言回调了loginView.loginSuccess()方法
        Mockito.verify(loginView).loginSuccess(anyString());
}
```

**注意：mock对象的方法有多个参数，如果其中一个参数使用any()系列方法，则其他所有参数都需要使用Matchers类的包装方法。** 假如我们上面断言回调了`restClient.login()`方法的代码这样写：  

`Mockito.verify(restClient).login("张三", "123", any(NetworkCallback.class));`  

看起来似乎没有什么问题，但是一运行便会报如下错误：

```java
This exception may occur if matchers are combined with raw values:
    //incorrect:
    someMethod(anyObject(), "raw String");
When using matchers, all arguments have to be provided by matchers.
For example:
    //correct:
    someMethod(anyObject(), eq("String by matcher"));
```

另外，如果想指定目标方法“抛出一个异常”，那么可以使用`Mockito.doThrow(desiredException)；`如果想让目标方法调用真实的逻辑，可以使用`Mockito.doCallRealMethod()`。

### 3、Spy对象
如果我们mock一个对象，但没有给方法指定特定行为，那么我们在使用这个对象的时候有一点需要注意： **mock对象的所有非void方法都将返回默认值：int、long类型方法将返回0，boolean方法将返回false，对象方法将返回null等等；而void方法将什么都不做，即使被mock对象的方法是一个非抽象方法，这个方法的逻辑也不会被执行。**  

那么很多时候，我们想到达这样的效果：除非指定，否者调用这个对象的默认实现，同时又能拥有验证方法调用的功能。咱们在改造一下之前的例子：

```java
public class NameVerifier{
    public boolean illegal(String name){
        return "法轮功".equals(name);
    }
}

public void setup(){
    restClient= Mockito.mock(RestClient.class);
    //<==将mock换成spy
    nameVerifier=Mockito.spy(NameVerifier.class);
    loginPresenter=new LoginPresenter2(restClient,nameVerifier);
    //<==把打桩的代码注释
    //Mockito.when(nameVerifier.illegal("张三")).thenReturn(false);
    //Mockito.when(nameVerifier.illegal("法轮功")).thenReturn(true);
}

@Test
public void testLoginSuccess(){
    loginPresenter.login("张三","123");
    Mockito.verify(restClient).login("张三","123");
}

```

总之，spy与mock的唯一区别就是默认行为不一样：spy对象的方法默认调用真实的逻辑，mock对象的方法默认什么都不做，或直接返回默认值。

## 练习
在项目中找一个mvc、mvp或mvvm的代码，使用Mockito进行测试

## 参考文献
* https://www.jianshu.com/p/b6e0cf81641b
* https://www.jianshu.com/p/a3b59fad17e6
* http://static.javadoc.io/org.mockito/mockito-core/2.18.3/org/mockito/Mockito.html
