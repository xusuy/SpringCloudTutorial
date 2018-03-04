# 原理剖析（第 002 篇）synchronized工作原理分析
-

## 一、大致介绍

``` 
1、用过synchronized的童鞋都知道这个关键字是Java中用于解决并发情况下数据的同步访问；
2、保证方法或者代码块在运行时，同一时刻只有一个方法可以进入到临界区，同时它还可以保证共享变量的内存可见性；
3、总的来说，其作用有三个特性：互斥性(确保线程互斥的访问同步代码)、可见性(保证共享变量的修改能够及时可见)、有序性(有效解决重排序问题)；
4、本章节就此和大家分享一下synchronized底层语义原理；
```


## 二、特性

### 2.1 互斥性

``` 
1、互斥性，可以认为独享的意思，每次只允许一个操作者拥有共享资源；

2、被synchronized修饰的代码块、实例方法、静态方法，多线程并发访问时，只能有一个线程获取到锁，其它线程都处于阻塞等待，
   但在此期间，这些线程仍然可以访问其它非synchronized修饰的方法；
```

### 2.2 可见性

``` 
1、可见性，就是每次线程的到来，都能访问到最新的值；

2、因为在互斥性的基础上，由于每次仅有一个线程执行临界区的代码，因此其修改的任何变量值对于稍后执行该临界区的线程来说是可见的；

3、再多说一句，因为互斥性的存在，也保证了临界区变量修改的原子性，而volatile仅仅只能保证变量修改的可见性，并不能保证原子性；
```


### 2.3 有序性

``` 
1、有序性，就是按照顺序来执行；

2、同样因为在互斥性的基础上，代码块也好，实例方法或静态方法也好，一旦被synchronized后，各个线程相互竞争，反正每次只能有一个线程执行；

3、打个比方，举例静态方法，TestSynchronized.java 中有个静态 synchronized static test(){ i++, j++} 方法，
   并且代码块被synchronized修饰，让N个线程都去调用这个方法，最后会发现每次i和j的输出值都是一样的。
   i++和j++要么一起执行完，要么都不执行，不会出现先i++后，执行了其他代码，过一会再执行j++的情况。
```


## 三、反编译查看字节码

### 3.1、反编译同步代码块

``` 
1、通过 javap -verbose 反编译代码块(反编译的汇编代码就不粘贴出来了)，最后会发现被反编译的代码块的前后被monitorenter、monitorexit一对指令包夹着；

2、关于这两条指令的作用，我们直接参考JVM规范中描述：

	monitorenter ：
	
	Each object is associated with a monitor. A monitor is locked if and only if it has an owner. The thread that executes monitorenter attempts to gain ownership of the monitor associated with objectref, as follows:
	• If the entry count of the monitor associated with objectref is zero, the thread enters the monitor and sets its entry count to one. The thread is then the owner of the monitor.
	• If the thread already owns the monitor associated with objectref, it reenters the monitor, incrementing its entry count.
	• If another thread already owns the monitor associated with objectref, the thread blocks until the monitor's entry count is zero, then tries again to gain ownership.


	monitorexit：　
	
	• The thread that executes monitorexit must be the owner of the monitor associated with the instance referenced by objectref.
	• The thread decrements the entry count of the monitor associated with objectref. If as a result the value of the entry count is zero, the thread exits the monitor and is no longer its owner. Other threads that are blocking to enter the monitor are allowed to attempt to do so.

3、monitorenter指令JVM规范翻译：
	每个对象有打自娘胎出来就自带一个内置监视器锁（monitor）。当monitor被占用时就会处于锁定状态，线程执行monitorenter指令时尝试获取monitor的所有权，过程如下：
	• 如果monitor的进入数为0，则该线程进入monitor，然后将进入数设置为1，该线程即为monitor的所有者。
	• 如果线程已经占有该monitor，只是重新进入，则进入monitor的进入数加1.
	• 如果其他线程已经占用了monitor，则该线程进入阻塞状态，直到monitor的进入数为0，再重新尝试获取monitor的所有权。

4、monitorexit指令JVM规范翻译：
	• 执行monitorexit的线程必须是objectref所对应的monitor的所有者。
	• 指令执行时，monitor的进入数减1，如果减1后进入数为0，那线程退出monitor，不再是这个monitor的所有者。其他被这个monitor阻塞的线程可以尝试去获取这个 monitor 的所有权。
```


### 3.2、反编译同步方法

``` 
1、通过 javap -verbose 反编译同步方法(反编译的汇编代码就不粘贴出来了)，最后会发现被反编译的同步方法附近有一个ACC_SYNCHRONIZED标示符；

2、关于ACC_SYNCHRONIZED指令的作用，我们直接参考JVM规范中描述：
	
	ACC_SYNCHRONIZED：

	Method-level synchronization is performed implicitly, as part of method invocation and return. 
	A synchronized method is distinguished in the run-time constant pool's methodinfo structure by the ACCSYNCHRONIZED flag, which is checked by the method invocation instructions. 
	When invoking a method for which ACC_SYNCHRONIZED is set, the executing thread enters a monitor, invokes the method itself, and exits the monitor whether the method invocation completes normally or abruptly. 
	During the time the executing thread owns the monitor, no other thread may enter it. 
	If an exception is thrown during invocation of the synchronized method and the synchronized method does not handle the exception, the monitor for the method is automatically exited before the exception is rethrown out of the synchronized method.

3、ACC_SYNCHRONIZED指令JVM规范翻译：
	• 方法级的同步是隐式的。同步方法的常量池中会有一个ACC_SYNCHRONIZED标志。
	• 当某个线程要访问某个方法的时候，会检查是否有ACC_SYNCHRONIZED，
	• 如果有设置，则需要先获得监视器锁，然后开始执行方法，方法执行之后再释放监视器锁。
	• 这时如果其他线程来请求执行方法，会因为无法获得监视器锁而被阻断住。
	• 值得注意的是，如果在方法执行过程中，发生了异常，并且方法内部并没有处理该异常，那么在异常被抛到方法外面之前监视器锁会被自动释放。
```



### 3.3、反编译小结

``` 
无论是monitorenter、 monitorexit，或者是ACC_SYNCHRONIZED，其都是基于Monitor实现的，因此接下来有必要了解下Monitor是什么东西，在了解Monitor的时候，我们还有必要了解下java对象头。
```




## 四、Java对象头、Monitor

### 4.1、Java对象头

``` 
1、在JVM中，对象在内存中的布局分为三块区域：对象头、实例数据和填充数据，这个在下面图3、图4、图5都可以看到布局图，例如“实例对象(对象锁)”；

2、而Java对象头里面包含Mark Word( 存储对象的hashCode、锁信息或分代年龄或GC标志等信息 )、Klass Pointer( 类型指针指向对象的类元数据，JVM通过这个指针确定该对象是哪个类的实例 )。

3、至于Mark Word里面具体包含了哪些信息，结构是怎么样的，请看下面图1；
```



### 4.2、Java对象头作用

``` 
1、Java头对象，它实现synchronized的锁对象的基础；

2、特别是Java对象头存储的锁信息，对synchronized的优化起到举足轻重的作用，其中轻量级锁、偏向锁是jdk6对synchronized锁进行优化后新增加的；

3、至此，如果对Java对象头还不理解的话，简单粗暴的讲，我们要依赖对象头Mark Word中的锁信息判断来决定如何优化synchronized；

4、如果还不理解的话，后面还会讲解到利用Java对象头的哪些信息是怎么做到synchronized锁优化的；
```



### 4.3、什么是Monitor？

``` 
Monitor其实是一种同步工具，也可以说是一种同步机制，它通常被描述为一个对象；

1、对象的所有方法都被“互斥”的执行。好比一个Monitor只有一个运行“许可”，任一个线程进入任何一个方法都需要获得这个“许可”，离开时把许可归还。

2、通常提供singal机制：允许正持有“许可”的线程暂时放弃“许可”，等待某个谓词成真（条件变量），而条件成立后，当前进程可以“通知”正在等待这个条件变量的线程，让他可以重新去获得运行许可。
```



### 4.4、Monitor粗俗理解

``` 
1、Java对象是天生的Monitor，每一个对象自打娘胎里出来，就带了一把看不见的锁，通常我们叫“内部锁”，或者“Monitor锁”，或者“Intrinsic lock”。

2、Java对象与这把内置锁紧密关联着，每个对象都存在着一个 monitor 与之关联，当锁升级为重量级锁时，监视器Monitor这把内置锁用来监视这些线程进入特殊的房间的,他的义务是保证（同一时间）只有一个线程可以访问被保护的数据和代码。

3、在Java虚拟机(HotSpot)中，monitor是由ObjectMonitor实现的，数据结构位于HotSpot虚拟机源码ObjectMonitor.hpp文件（http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/f2110083203d/src/share/vm/runtime/objectMonitor.hpp）；
```



### 4.5、Java对象头、Monitor小结

``` 
1、讲到这里，大家对synchronized的原理所涉及到的一些知识点有了大概的了解，那他们究竟是如何互相作用将synchronized玩转的呢？

2、在jdk6之前，起主导作用的仍然是重量级锁，但是随着jdk6的问世，synchronized添加偏向锁、轻量级锁，优化了synchronized锁的获取方式；

3、对于“无锁->偏向锁->轻量级锁”的转变，我们主要看线程的栈帧、Java对象头，源码在jdk8的synchronizer.cpp中(http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/tip/src/share/vm/runtime/synchronizer.cpp)；

4、对于“轻量级锁->重量级锁”，我们主要看Monitor，源码在jdk8的objectMonitor.cpp中(http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/tip/src/share/vm/runtime/objectMonitor.cpp)；
```


## 五、锁的类型
	无锁->偏向锁->轻量级锁->重量级锁，它会随着竞争情况逐渐升级。锁可以升级但不能降级，目的是为了提高获得锁和释放锁的效率。


### 5.1、偏向锁


``` 
1、引入原由：大多数情况下，锁的竞争关系都是单一的，锁由同一个线程多次获取，为了降低获取锁的代价才引入了偏向锁，减少了一些不必要的CAS操作；

2、偏向锁加锁流程：
线程请求获取锁 ----→ 目前锁状态为01
					   ¦
					   ¦
					   ↓           N
				   是否偏向锁? ------------→ 利用 CAS 操作替换 Mark Word 的线程ID
					   ¦					   ↑	¦					¦
					   ¦Y					  ¦	¦					¦N
					   ↓					N  ¦	¦					↓			      Y
	¦--→	检查Mark Word是否记录当前线程ID? -----¦    ¦Y		 检测对象锁的锁偏向是否偏向? ----------→ 尝试CAS替换线程ID，
	¦				   ¦					   	¦					¦					     此时会引发竞争，升级为轻量级锁
	¦				   ¦Y					  	¦					¦
	¦				   ↓					   	¦					¦
	¦		获得偏向锁并将锁的偏向改为1  ←-------------¦					¦
	¦				   ¦												¦
	¦				   ¦												¦N
	¦				   ↓												¦
	¦			  执行同步代码块									  	¦
	¦															    	¦
	¦																	¦
	¦--------------------------------------------------------------------¦											

``` 


### 5.2、轻量级锁

``` 
1、引入原由：在无锁竞争的情况下完全可以避免调用操作系统层面的重量级互斥锁，取而代之的是在monitorenter和monitorexit中只需要依靠一条CAS原子指令就可以完成锁的获取及释放。
   当存在锁竞争的情况下，执行CAS指令失败的线程将调用操作系统互斥锁进入到阻塞状态，当锁被释放的时候被唤醒。

2、轻量级锁加锁流程：
升级为轻量级锁 ----→ 原持有偏向锁的线程在线程栈帧分配锁记录Lock Record			 ¦------→  当前线程栈帧分配锁记录
									   ¦									¦				¦
									   ¦									¦				¦
									   ↓									¦				↓
				拷贝对象锁中的对象头的Mark Word到Lock Record的Header中		  ¦	拷贝对象头的Mark Word到锁记录中			  再尝试
									   ¦									¦				¦						¦------------¦
									   ¦									¦				¦						¦			¦
									   ↓					      当前线程   ¦				↓						↓			¦
					原持有偏向锁的线程获得轻量级锁，锁标志位改为00  --------------¦	CAS操作，将对象头的锁记录指针指向当前线程锁记录? ----→  自旋计数N次
									   ¦													 ¦										¦
									   ¦													 ¦Y									   ¦
									   ↓													 ↓										↓
								原持有偏向锁的线程									 当前线程获得轻量级锁						  N次CAS还是失败的话，
									   ¦													 ¦								  则升级为重量级锁
									   ¦													 ¦
									   ↓													 ↓
							   安全点执行同步代码块										执行同步代码块
									   ¦													 ¦
									   ¦													 ¦
									   ¦													 ↓
									   ¦-------------------------------------------→   开始轻量级锁解锁
			








``` 


### 5.3、重量级锁
↓  →   ↑   ←  ¦  ↔  ↕ ↨ ► ▼ ◄ ▲ ▬
``` 
1、重量级锁就不用讲了，一直沿用至今，主要利用内置锁，内置锁的本质是依赖操作系统，因此内置锁对各个线程的阻塞是由操作系统完成（在Linxu下通过pthread_mutex_lock函数）；

2、重量级锁加锁流程：
											当前线程							 Y
升级为重量级锁 ----→ 原有线程获得重量级锁，  --------------→  内置锁_owner==null? ------→  当前线程获取到锁
				Mark Word指向内置锁Monitor指针			  ↑		 ¦
														  ¦		 ¦N
														  ¦		 ↓				   Y
														  ¦	_owner==self当前线程?  -------→  重入锁获取到锁
														  ¦		 ¦
														  ¦		 ¦N
														  ¦---------¦
															自旋	 ¦
																	¦
																	¦
															 尝试自旋仍未获得锁
																	¦
																	¦
																	↓
										将线程封装成ObjectWaiter插入_cxq队列中，CAS将_cxq指向该ObjectWaiter
																	¦
																	¦
																	↓
																 part挂起

``` 


### 5.4、锁分类小结

``` 
1、至此，各个锁的加锁操作已经差不多了解了一番，此刻我们应该改正monitorenter指令就是获取对象重量级锁的错误认识，很显然，优化之后，锁的获取判断次序是偏向锁->轻量级锁->重量级锁。

2、偏向锁重要的两个方法fast_enter、fast_exit，jdk源码路径(http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/tip/src/share/vm/runtime/synchronizer.cpp);

3、轻量级锁重要的两个方法slow_enter、slow_exit，jdk源码路径(http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/tip/src/share/vm/runtime/synchronizer.cpp);

4、重量级锁重要的三个方法enter、EnterI、exit，jdk源码路径(http://hg.openjdk.java.net/jdk8/jdk8/hotspot/file/tip/src/share/vm/runtime/objectMonitor.cpp);
``` 

 


## 六、synchronized锁总结图片
![](https://i.imgur.com/zyIRZZr.png)



## 七、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!





























