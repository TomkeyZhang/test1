## Kotlin推荐学习路线
1. 到[Android官网](https://developer.android.com/kotlin/)查看使用Kotlin开发android应用的优势
2. 到[kotlin官网](https://www.kotlincn.net/docs/reference/basic-syntax.html)快速学习基本语法
3. 推荐阅读[这篇文章](http://ju.outofmemory.cn/entry/355287)(有疑问到官网查看相应文档 [英文](http://kotlinlang.org/docs/reference/)，[中文](https://www.kotlincn.net/docs/reference/))的语言特性总结部分，可以了解kotlin的主要特性。注意这篇文章有3处错误:
 - Secondary constructor - 属性和构造函数换行 
 - 属性和访问方法 - set()/get() 应该将name改为field  
 - lambda 表达式 - eval{ x * x } 应该是 eval{ it * it }
4. 每天花一些时间（比如：1个小时）使用kotlin官方的[练习仓库](https://github.com/Kotlin/kotlin-koans) 来熟悉kotlin，这个库以单元测试的方式设计了42个小任务，需要通过编写代码来使单元测试通过
5. 在项目中开始用kotlin写单元测试
6. 阅读kotlin官网关于Android开发的准备内容[使用Kotlin Android 扩展](https://www.kotlincn.net/docs/tutorials/android-plugin.html) [处理Android框架](https://www.kotlincn.net/docs/tutorials/android-frameworks.html)
7. 推荐阅读 kotlin-for-android-developers-zh ，查看[源代码](https://github.com/antoniolg/Kotlin-for-Android-Developers)，可以不必练习，通读以后写Android的kotlin代码时遇到不知道该怎么写的可以直接拿来参考
8. 开始用kotlin在项目中写正式代码
