#测试入门之JUnit测试框架
## 为什么需要单元测试框架
还是我们上次分享的计算器的例子，如果不用测试框架我们要如何来做测试？

```java
public class Calculator {
    public int add(int one, int another) {
        return one + another;
    }
    
    public int multiply(int one, int another) {
        return one * another;
    }
}
```

```java
public class CalculatorTestWithoutJUnit {
    public void testAdd() {
        Calculator calculator = new Calculator();
        int sum = calculator.add(2, 3);
        if (sum == 5) {
            System.out.println("add() SUCCESS!");
        } else {
            System.out.println("add() FAIL!");
        }
    }

    public void testMultiply() {
        Calculator calculator = new Calculator();
        int product = calculator.multiply(2, 3);
        if (product == 6) {
            System.out.println("multiply() SUCCESS!");
        } else {
            System.out.println("multiply() FAILs!");
        }
    }

    public static void main(String[] args) {
        CalculatorTestWithoutJUnit testWithoutJUnit = new CalculatorTestWithoutJUnit();
        testWithoutJUnit.testAdd();
        testWithoutJUnit.testMultiply();
    }
}
```
这样不仅代码重复，还需要用肉眼去判断打印的结果是否正确，但是如果有大量的测试要执行，这种方式就显然不可取。于是就有了测试框架JUnit，然后我们就可以这样写：

```java
public class CalculatorTest {
    Calculator calculator;
    @Before
    public void setUp() throws Exception {
        calculator = new Calculator();
    }

    @Test
    public void testAdd() throws Exception {
        Assert.assertEquals(5, calculator.add(2,3));
    }

    @Test
    public void testMultiply() throws Exception {
        Assert.assertEquals(6, calculator.multiply(2,3));
    }

    @After
    public void tearDown() throws Exception {
        calculator = null;
    }
}
```
执行完我们就可以很容易通过颜色来看出执行的结果。
## JUnit常用注解
* @Test，最小测试方法注解。
* @BeforeClass，测试类执行前操作，用于初始化，必须是public static void，且放在类的开始处
* @AfterClass，测试类执行后操作，用于清理、释放，必须是public static void，且放在类的开始处
* @Before，@Test方法执行前逻辑
* @After，@Test方法执行后逻辑
* @Ignored，忽略@Test方法执行

## 断言-验证结果
在JUnit中我们使用`Assert`类的assert系列方法来验证测试结果，最常用的有：


`assertEquals(expected, actual)`  
&nbsp;验证expected和actual的值是否相等，如果他们是对象，则通过equals()方法来判断

`assertEquals(expected, actual, tolerance)`  
&nbsp;这里传入的expected和actual是float或double类型的，大家知道计算机表示浮点型数据都有一定的偏差，所以哪怕理论上他们是相等的，但是用计算机表示出来则可能不是，所以这里运行传入一个偏差值。如果两个数的差异在这个偏差值之内，则测试通过，否者测试失败

`assertTrue(boolean condition)`   
 &nbsp;验证contidion的值是true

`assertFalse(boolean condition)`   
 &nbsp;验证contidion的值是false

`assertNull(Object obj)`   
 &nbsp;验证obj的值是null

`assertNotNull(Object obj)`   
 &nbsp;验证obj的值不是null

`assertSame(expected, actual)`  
 &nbsp;验证expected和actual是同一个对象，即指向同一个对象

`assertNotSame(expected, actual)`  
 &nbsp;验证expected和actual不是同一个对象，即指向不同的对象


上面的每一个方法，都有一个重载的方法，可以在前面加一个String类型的参数，表示如果验证失败的话，将用这个字符串作为失败的结果报告.比如：`assertEquals("Current user Id should be 1", 1, currentUser.id());` 
当`currentUser.id()`的值不是1的时候，在结果报道里面将显示"Current user Id should be 1"，这样可以让测试结果更具有可读性，更清楚错误的原因是什么。

>> 如果要验证一个方法是否抛出了异常怎么处理呢？@Test 注解有一个属性：expected，用于断言当前方法会抛出相应的异常。


## 练习
* 下面是一个将字符串按指定长度进行分割的方法，对它进行单元测试

```java
public static List<String> splitWithLength(String source, int length) {
        List<String> list = new ArrayList<>();
        int start = 0;
        while (start < source.length()) {
            int end = start + length;
            if (end > source.length()) {
                end = source.length();
            }
            list.add(source.substring(start, end));
            start = end;
        }
        return list;
    }
```

* 在项目中找一个类，使用JUnit至少测试其中的一个方法，要求覆盖该方法所有边界情况。

>> 如：com.tomkey.commons.tools.Strings

练习分支：mayflower-unittest，knight-unittest，bdtool-unittest

## 参考文章
* https://www.jianshu.com/p/e43e56667d9d
* https://www.jianshu.com/p/eb82a277f222
* https://www.jianshu.com/p/c821a315c6aa
