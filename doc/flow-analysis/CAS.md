# 原理剖析（第 004 篇）CAS工作原理分析
-

## 一、大致介绍

``` 
1、关于多线程竞争锁方面，大家都知道有个CAS和AQS，也正是这两个东西才引申出了大量的线程安全类，锁类等功能；
2、而随着现在的硬件厂商越来越高级，在硬件层面提供大量并发原语给我们Java层面的开发带来了莫大的利好；
3、本章节就和大家分享分析一下CAS的工作原理；
```


## 二、原理分析

### 2.1 何为CAS？

``` 
1、CAS,compare and swap的缩写，顾名思义，比较再交换，即 “读取-修改-写操作” 三个步骤为一体原子操作；

2、CAS操作包含三个参数：内存位置(V)、预期值(A)、新值(B)；
   如果内存位置的值V与预期值A相匹配，那么处理器会自动将该位置值更新为新值B，否则不更新；
```

### 2.2 CAS原理

``` 
1、CAS通过JNI方式调用底层操作系统的C代码，从而借助底层C代码来调用CPU底层操作指令来实现原子操作；

2、CAS是硬件CPU提供的原语，通过底层cmpxchg原语指令(多处理器再加上Lock指令)实现原子操作；
```



### 2.3 CAS核心源码

``` 
1、CAS核心源码：
// Adding a lock prefix to an instruction on MP machine
// VC++ doesn't like the lock prefix to be on a single line
// so we can't insert a label after the lock prefix.
// By emitting a lock prefix, we can define a label after it.
#define LOCK_IF_MP(mp) __asm cmp mp, 0  \
                       __asm je L0      \
                       __asm _emit 0xF0 \
                       __asm L0:

inline jint     Atomic::cmpxchg    (jint     exchange_value, volatile jint*     dest, jint     compare_value) {
  // alternative for InterlockedCompareExchange
  int mp = os::is_MP();
  __asm {
    mov edx, dest
    mov ecx, exchange_value
    mov eax, compare_value
    LOCK_IF_MP(mp) // 如果是多处理器的话，则需要添加Lock前缀指令，Lock的方式和Volatile的实现方式雷同
    cmpxchg dword ptr [edx], ecx // 
  }
}

2、通过上述源码可以发现该cmpxchg方法会自动判断当前是否是多处理器，多处理器的话则添加lock前缀指令，反之省略lock前缀；

3、至于lock是怎么保证多处理器的一致性的话，原理和Volatile雷同，请移步看看[原理剖析（第 001 篇）Volatile工作原理分析]；
```



## 三、CAS缺点

### 3.1、ABA问题

``` 
1、并发操作时，容易引起ABA问题；
   假设i初始值i=5，A线程做i++操作一次，B线程做i--操作一次，C线程通过判断i=5时则对i进行更新新值；

2、这个时候C线程会认为i还是处于初始值，未被做过修改，但是殊不知AB线程已经都对i进行修改了一次；

3、为了解决这种线程，需要让C知道i已经被修改过了，因此在Java1.5引进了一个AtomicStampedReference类来解决ABA问题；

4、AtomicStampedReference这个类主要是给变量追加了版本号信息，每次变量更新的话版本号都会自增加一；

5、但是有的人会认为AtomicMarkableReference也能解决ABA问题，其实不能根本解决只能在最大程度上降低ABA问题的出现；
   因为它是通过一个boolean来标记是否更改，本质就是只有true和false两种版本来回切换，只能降低ABA问题发生的几率，并不能阻止ABA问题的发生；
```


### 3.2、开销大

``` 
1、随便拿个CAS的Java层代码：
    public final int getAndSetInt(Object var1, long var2, int var4) {
        int var5;
        do {
            var5 = this.getIntVolatile(var1, var2);
        } while(!this.compareAndSwapInt(var1, var2, var5, var4));

        return var5;
    }

2、通过这段代码发现，如果CAS操作一直不成功的话，那么该段代码就一直在自旋操作，会给CPU带来比较大的执行开销；
```



### 3.3、原子操作约束

``` 
1、目前的CAS只能保证单个共享变量的原子操作；

2、但是对多个变量进行操作时，CAS无法保证，但是可以将多个变量封装成一个新的对象，利用AtomicReference类来保证引用对象之间的原子性；
```



## 四、总结
``` 
1、我们可以在一些非常简单的操作且又不想引入锁的场景下采用CAS实现原子操作；

2、然而想要进行非阻塞的完成某些场景也可以考虑采用CAS进行原子操作；

3、但是不推荐在非常复杂的操作中引入CAS，一来会使程序可读性变差，二来且难以测试且会出现ABA问题。
``` 




## 五、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!