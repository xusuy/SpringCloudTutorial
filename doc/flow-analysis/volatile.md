# 原理剖析（第 001 篇）Volatile工作原理分析
-

## 一、大致介绍

``` 
1、用过这个关键字的童鞋都知道，都知道这个关键字很强大，主要作用是保证变量在多线程之间的可见性；
2、volatile在concurrent包中起着举足轻重的作用，为大量的并发类提供了有力的援助；
3、接下来我们从了解CPU缓存开始，然后再深入原理剖析，循序渐进的了解volatile；
```


## 二、CPU缓存

### 2.1 传输链路

``` 
CPU（线程） 
--》  CPU缓存（一级、二级、三级缓存等）  
--》  主内存

大致的传输方向就这样，而且还是必须是双向传输。
```

### 2.2 CPU缓存

``` 
1、CPU缓存解决了CPU运算速度与内存读者速度不匹配的问题；

2、因为主内存访问通常比较慢，访问时间大概在几十到几百个时钟，而CPU缓存还有一二三级之分，每个级别的读者速度虽然很快，
   但还是有访问速度的区分，至少比主内存的读者速度快很多，所以CPU缓存它的出现在很大程度上提高了数据之间的传输；

3、每一级缓存中所存储的数据全部都是下一级缓存中的一部分，这三种缓存的技术难度和制造成本是相对递减的，所以其容量也相对递增；

4、当CPU要读取一个数据时，首先从一级缓存中查找，如果没有再从二级缓存中查找，如果还是没有再从三级缓存中或内存中查找。
   一般来说每级缓存的命中率大概都有80%左右，也就是说全部数据量的80%都可以在一级缓存中找到；
```


## 三、原理特性

### 3.1、可见性

``` 
1、浅显的讲，不论线程是如何如何的访问带volatile字段的对象，都会访问到内存中最新的一份值，这就是可见性的大致阐述；

2、具体的讲，当我们在java代码中书写的那行对volatile对象进行写操作时，JVM会向处理器发送一条Lock指令，
   Lock指令锁住（锁总线）确保变量对象所在缓存行数据会更新到主内存中去，确保更新后如果再有其他线程访问该对象，
   其他线程一律强制从主内存中重新读取最新的值。

3、因为所有内存的传输都发生在一条共享的总线上，并且所有的处理器都能看到这条总线，那么既然所有处理器都能看到这条总线，
   总不至于看见了不干点啥吧？

4、没错，每个处理器都会通过一种嗅探技术，不停的嗅探总线上传输的数据，以便来检查自己缓存中的数据是否过期。
   当处理器发现高速缓存中的数据对应的内存地址被修改，会将该缓存数据置为失效，当处理器下次访问该内存地址
   数据时，将强制重新从系统内存中读取。

5、而且CPU制造商也曾制定了一个这样的规则：当一个CPU修改缓存中的字节对象时，服务器中其他CPU会被通知，它们的缓存将视为无效。
   当那些被视为无效变量所在的线程再次访问字节对象时，则强制再次从主内存中获取最新值。

6、至于第2点提到Lock锁总线，其实最初采用锁总线，虽说能解决问题，但是效率地下，一旦锁总线，其他CPU就得干等着，
   光看不干效率不行嘛。所以后来优化成了锁缓存，效率也高了，开销也自然就少了，总之Lock目的很明确，确保锁住的那份值最新，
   且其他持有该缓存的备份处都得失效，其实这种锁缓存过程的思想也正是缓存一致性协议的核心思想。

7、综上所述，所以不论何时不论何地在哪种多线程环境下，只要你想获取被volatile修饰过的字段，都能看到最新的一份值，
   这就是可见性的终极描述。
```


### 3.2、有序性

``` 
1、浅显的讲，A1，A2，A3三块代码先后执行，A2有一行代码被volatile修饰过，那么在被反编译成指令进行重排序时，A2必须等到A1
   执行完了才能开始，但是A1内部的指令可以支持重排指令；而A3代码块的执行必须等到A2执行完了才能开始，但是A3内部的指令可以支持
   重排指令，这就是有序性，只要A2夹在中间，A2必须等A1执行完才能干活，A2没干完活，A3是不允许开工的。

2、具体的讲，Lock前缀指令实际上相当于一个内存屏障（也成内存栅栏），它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，
   也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成。

3、综上所述，有序不是我们通常说的自然顺序，而是在有volatile修饰时，存在类似尊卑等级的先后有序这么一说。
```



### 3.3、非原子性

``` 
1、本不该拿到台面上讲是不是属于volatile的特性，因为我们不能认为仅仅只是因为可见性随处都是最新值，那么就认为是原子性操作。

2、对于第1种想法，简直是大错特错，因为可见性只是将volatile变量这回主内存并使得其他CPU缓存失效，但是不带代表对volatile变量
   回写主内存的动作和对volatile变量的逻辑操作是捆绑在一起的。因此既要逻辑操作，又要写回主内存，这本来就违背了volatile特性
   的本意，所以volatile并不是原子操作的。
```





## 四、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























