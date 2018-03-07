# 原理剖析（第 003 篇）ThreadPoolExecutor工作原理分析
-

## 一、大致介绍

``` 
1、相信大家都用过线程池，对该类ThreadPoolExecutor应该一点都不陌生了；
2、我们之所以要用到线程池，线程池主要用来解决线程生命周期开销问题和资源不足问题；
3、我们通过对多个任务重用线程以及控制线程池的数目可以有效防止资源不足的情况；
4、本章节就着重和大家分享分析一下JDK8的ThreadPoolExecutor核心类，看看线程池是如何工作的；
```


## 二、基本字段方法介绍

### 2.1 构造器

``` 
1、四个构造器：
	// 构造器一
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }
	
	// 构造器二
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory, defaultHandler);
    }

	// 构造器三
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }

	// 构造器四
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

2、通过仔细查看构造器代码，发现最终都是调用构造器四，紧接着赋值了一堆的字段，接下来我们先看看这些字段是什么含义；
```

### 2.2 成员变量字段

``` 
1、corePoolSize：核心运行的线程池数量大小，当线程数量超过该值时，就需要将超过该数量值的线程放到等待队列中；

2、maximumPoolSize：线程池最大能容纳的线程数(该数量已经包含了corePoolSize数量)，当线程数量超过该值时，则会拒绝执行处理策略；

3、workQueue：等待队列，当达到corePoolSize的时候，就将新加入的线程追加到workQueue该等待队列中；
			  当然BlockingQueue类也是一个抽象类，也有很多子类来实现不同的队列等待；
			  
			  一般来说，阻塞队列有一下几种，ArrayBlockingQueue;LinkedBlockingQueue/SynchronousQueue/ArrayBlockingQueue/
			  PriorityBlockingQueue使用较少，一般使用LinkedBlockingQueue和Synchronous。

4、keepAliveTime：表示线程没有任务执行时最多保持多久存活时间，默认情况下当线程数量大于corePoolSize后keepAliveTime才会起作用
				  并生效，一旦线程池的数量小于corePoolSize后keepAliveTime又不起作用了；
				  
				  但是如果调用了allowCoreThreadTimeOut(boolean)方法，在线程池中的线程数不大于corePoolSize时，
				  keepAliveTime参数也会起作用，直到线程池中的线程数为0；

5、threadFactory：新创建线程出生的地方；

6、handler：拒绝执行处理抽象类，就是说当线程池在一些场景中，不能处理新加入的线程任务时，会通过该对象处理拒绝策略；
			该对象RejectedExecutionHandler有四个实现类，即四种策略，让我们有选择性的在什么场景下该怎么使用拒绝策略；
			策略一( CallerRunsPolicy )：只要线程池没关闭，就直接用调用者所在线程来运行任务；
			策略二( AbortPolicy )：默认策略，直接抛出RejectedExecutionException异常；
			策略三( DiscardPolicy )：执行空操作，什么也不干，拒绝任务后也不做任何回应；
			策略四( DiscardOldestPolicy )：将队列中存活最久的那个未执行的任务抛弃掉，然后将当前新的线程放进去；
   
7、largestPoolSize：变量记录了线程池在整个生命周期中曾经出现的最大线程个数；

8、allowCoreThreadTimeOut：当为true时，和弦线程也有超时退出的概念一说；
```



### 2.3 成员方法

``` 
1、AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
   // 原子变量值，是一个复核类型的成员变量，是一个原子整数，借助高低位包装了两个概念：
   
2、int COUNT_BITS = Integer.SIZE - 3;
   // 偏移位数，常量值29，之所以偏移29，目的是将32位的原子变量值ctl的高3位设置为线程池的状态，低29位作为线程池大小数量值；
	
3、int CAPACITY   = (1 << COUNT_BITS) - 1;
   // 线程池的最大容量值；
	
4、线程池的状态，原子变量值ctl的高三位：
   int RUNNING    = -1 << COUNT_BITS; // 接受新任务，并处理队列任务
   int SHUTDOWN   =  0 << COUNT_BITS; // 不接受新任务，但会处理队列任务
   int STOP       =  1 << COUNT_BITS; // 不接受新任务，不会处理队列任务，而且会中断正在处理过程中的任务
   int TIDYING    =  2 << COUNT_BITS; // 所有的任务已结束，workerCount为0，线程过渡到TIDYING状态，将会执行terminated()钩子方法
   int TERMINATED =  3 << COUNT_BITS; // terminated()方法已经完成
   
5、HashSet<Worker> workers = new HashSet<Worker>(); 
   // 存放工作线程的线程池；
```


### 2.4 成员方法

``` 
1、public void execute(Runnable command)
   // 提交任务，添加Runnable对象到线程池，由线程池调度执行

2、private static int workerCountOf(int c)  { return c & CAPACITY; }
   // c & 高3位为0，低29位为1的CAPACITY，用于获取低29位的线程数量

3、private boolean addWorker(Runnable firstTask, boolean core)
   // 添加worker工作线程，根据边界值来决定是否创建新的线程

4、private static boolean isRunning(int c)
   // c通常一般为ctl，ctl值小于0，则处于可以接受新任务状态

5、final void reject(Runnable command) 
   // 拒绝执行任务方法，当线程池在一些场景中，不能处理新加入的线程时，会通过该对象处理拒绝策略；
   
6、final void runWorker(Worker w)
   // 该方法被Worker工作线程的run方法调用，真正核心处理Runable任务的方法

7、private static int runStateOf(int c)     { return c & ~CAPACITY; }
   // c & 高3位为1，低29位为0的~CAPACITY，用于获取高3位保存的线程池状态
   
8、public void shutdown()
   // 不会立即终止线程池，而是要等所有任务缓存队列中的任务都执行完后才终止，但再也不会接受新的任务

9、public List<Runnable> shutdownNow()
   // 立即终止线程池，并尝试打断正在执行的任务，并且清空任务缓存队列，返回尚未执行的任务
   
10、private void processWorkerExit(Worker w, boolean completedAbruptly)
   // worker线程退出   
```


## 三、源码分析

### 3.1、execute

``` 
1、execute源码：
   /**
     * Executes the given task sometime in the future.  The task
     * may execute in a new thread or in an existing pooled thread.
     *
     * If the task cannot be submitted for execution, either because this
     * executor has been shutdown or because its capacity has been reached,
     * the task is handled by the current {@code RejectedExecutionHandler}.
     *
     * @param command the task to execute
     * @throws RejectedExecutionException at discretion of
     *         {@code RejectedExecutionHandler}, if the task
     *         cannot be accepted for execution
     * @throws NullPointerException if {@code command} is null
     */
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * Proceed in 3 steps:
         *
         * 1. If fewer than corePoolSize threads are running, try to
         * start a new thread with the given command as its first
         * task.  The call to addWorker atomically checks runState and
         * workerCount, and so prevents false alarms that would add
         * threads when it shouldn't, by returning false.
         *
         * 2. If a task can be successfully queued, then we still need
         * to double-check whether we should have added a thread
         * (because existing ones died since last checking) or that
         * the pool shut down since entry into this method. So we
         * recheck state and if necessary roll back the enqueuing if
         * stopped, or start a new thread if there are none.
         *
         * 3. If we cannot queue task, then we try to add a new
         * thread.  If it fails, we know we are shut down or saturated
         * and so reject the task.
         */
        int c = ctl.get(); // 获取原子计数值最新值
        if (workerCountOf(c) < corePoolSize) { // 判断当前线程池数量是否小于核心线程数量
            if (addWorker(command, true)) // 尝试添加command任务到核心线程
                return;
            c = ctl.get(); // 重新获取当前线程池状态值，为后面的检查做准备。
        }
		// 执行到此，说明核心线程任务数量已满，新添加的线程入等待队列，这个熟练是大于corePoolSize且小于maximumPoolSize
        if (isRunning(c) && workQueue.offer(command)) { // 如果线程池处于可接受任务状态，尝试添加到等待队列
            int recheck = ctl.get(); // 双重校验
            if (! isRunning(recheck) && remove(command)) // 如果线程池突然不可接受任务，则尝试移除该command任务
                reject(command); // 不可接受任务且成功从等待队列移除任务，则执行拒绝策略操作，通过策略告诉调用方任务入队情况
            else if (workerCountOf(recheck) == 0) // 如果此刻线程数量为0的话将没有Worker执行新的task，所以增加一个Worker
                addWorker(null, false); // 添加一个Worker
        }
		// 执行到此，说明添加任务等待队列已满，所以尝试添加一个Worker
        else if (!addWorker(command, false)) // 如果添加失败的话，那么拒绝此线程任务添加
            reject(command); // 拒绝此线程任务添加
    }
	
2、小结：
	• 如果线程池中的线程数量 < corePoolSize，就创建新的线程来执行新添加的任务；
	• 如果线程池中的线程数量 >= corePoolSize，但队列workQueue未满，则将新添加的任务放到workQueue中；
	• 如果线程池中的线程数量 >= corePoolSize，且队列workQueue已满，但线程池中的线程数量 < maximumPoolSize，则会创建新的线程来处理被添加的任务；
	• 如果线程池中的线程数量 = maximumPoolSize，就用RejectedExecutionHandler来执行拒绝策略；
```


### 3.2、addWorker

``` 
1、addWorker源码：
   /**
     * Checks if a new worker can be added with respect to current
     * pool state and the given bound (either core or maximum). If so,
     * the worker count is adjusted accordingly, and, if possible, a
     * new worker is created and started, running firstTask as its
     * first task. This method returns false if the pool is stopped or
     * eligible to shut down. It also returns false if the thread
     * factory fails to create a thread when asked.  If the thread
     * creation fails, either due to the thread factory returning
     * null, or due to an exception (typically OutOfMemoryError in
     * Thread.start()), we roll back cleanly.
     *
     * @param firstTask the task the new thread should run first (or
     * null if none). Workers are created with an initial first task
     * (in method execute()) to bypass queuing when there are fewer
     * than corePoolSize threads (in which case we always start one),
     * or when the queue is full (in which case we must bypass queue).
     * Initially idle threads are usually created via
     * prestartCoreThread or to replace other dying workers.
     *
     * @param core if true use corePoolSize as bound, else
     * maximumPoolSize. (A boolean indicator is used here rather than a
     * value to ensure reads of fresh values after checking other pool
     * state).
     * @return true if successful
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry: // 外层循环，负责判断线程池状态，处理线程池状态变量加1操作
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c); // 读取状态值

            // Check if queue empty only if necessary.
			// 满足下面两大条件的，说明线程池不能接受任务了，直接返回false处理
			// 主要目的就是想说，只有线程池的状态为 RUNNING 状态时，线程池才会接收新的任务，增加新的Worker工作线程
            if (rs >= SHUTDOWN && // 线程池的状态已经至少已经处于不能接收任务的状态了，目的是检查线程池是否处于关闭状态
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

			// 内层循环，负责worker数量加1操作
            for (;;) {
                int wc = workerCountOf(c); // 获取当前worker线程数量
                if (wc >= CAPACITY || // 如果线程池数量达到最大上限值CAPACITY
					// core为true时判断是否大于corePoolSize核心线程数量
					// core为false时判断是否大于maximumPoolSize最大设置的线程数量
                    wc >= (core ? corePoolSize : maximumPoolSize)) 
                    return false;
					
				// 调用CAS原子操作，目的是worker线程数量加1
                if (compareAndIncrementWorkerCount(c)) // 
                    break retry;
					
                c = ctl.get();  // Re-read ctl // CAS原子操作失败的话，则再次读取ctl值
                if (runStateOf(c) != rs) // 如果刚刚读取的c状态不等于先前读取的rs状态，则继续外层循环判断
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
				// 之所以会CAS操作失败，主要是由于多线程并发操作，导致workerCount工作线程数量改变而导致的，因此继续内层循环尝试操作
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
			// 创建一个Worker工作线程对象，将任务firstTask，新创建的线程thread都封装到了Worker对象里面
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
				// 由于对工作线程集合workers的添加或者删除，涉及到线程安全问题，所以才加上锁且该锁为非公平锁
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
					// 获取锁成功后，执行临界区代码，首先检查获取当前线程池的状态rs
                    int rs = runStateOf(ctl.get());

					// 当线程池处于可接收任务状态
					// 或者是不可接收任务状态，但是有可能该任务等待队列中的任务
					// 满足这两种条件时，都可以添加新的工作线程
                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w); // 添加新的工作线程到工作线程集合workers，workers是set集合
                        int s = workers.size();
                        if (s > largestPoolSize) // 变量记录了线程池在整个生命周期中曾经出现的最大线程个数
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) { // 往workers工作线程集合中添加成功后，则立马调用线程start方法启动起来
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted) // 如果启动线程失败的话，还得将刚刚添加成功的线程共集合中移除并且做线程数量做减1操作
                addWorkerFailed(w);
        }
        return workerStarted;
    }
	
2、小结：
	• 该方法是任务提交的一个核心方法，主要完成状态的检查，工作线程的创建并添加到线程集合切最后顺利的话将创建的线程启动；
	• addWorker(command, true)：当线程数小于corePoolSize时，添加一个需要处理的任务command进线程集合，如果workers数量超过corePoolSize时，则返回false不需要添加工作线程；
	• addWorker(command, false)：当等待队列已满时，将新来的任务command添加到workers线程集合中去，若线程集合大小超过maximumPoolSize时，则返回false不需要添加工作线程；
	• addWorker(null, false)：放一个空的任务进线程集合，当这个空任务的线程执行时，会从等待任务队列中通过getTask获取任务再执行，创建新线程且没有任务分配，当执行时才去取任务；
	• addWorker(null, true)：创建空任务的工作线程到workers集合中去，在setCorePoolSize方法调用时目的是初始化核心工作线程实例；
```



### 3.3、runWorker

``` 
1、runWorker源码：
    /**
     * Main worker run loop.  Repeatedly gets tasks from queue and
     * executes them, while coping with a number of issues:
     *
     * 1. We may start out with an initial task, in which case we
     * don't need to get the first one. Otherwise, as long as pool is
     * running, we get tasks from getTask. If it returns null then the
     * worker exits due to changed pool state or configuration
     * parameters.  Other exits result from exception throws in
     * external code, in which case completedAbruptly holds, which
     * usually leads processWorkerExit to replace this thread.
     *
     * 2. Before running any task, the lock is acquired to prevent
     * other pool interrupts while the task is executing, and then we
     * ensure that unless pool is stopping, this thread does not have
     * its interrupt set.
     *
     * 3. Each task run is preceded by a call to beforeExecute, which
     * might throw an exception, in which case we cause thread to die
     * (breaking loop with completedAbruptly true) without processing
     * the task.
     *
     * 4. Assuming beforeExecute completes normally, we run the task,
     * gathering any of its thrown exceptions to send to afterExecute.
     * We separately handle RuntimeException, Error (both of which the
     * specs guarantee that we trap) and arbitrary Throwables.
     * Because we cannot rethrow Throwables within Runnable.run, we
     * wrap them within Errors on the way out (to the thread's
     * UncaughtExceptionHandler).  Any thrown exception also
     * conservatively causes thread to die.
     *
     * 5. After task.run completes, we call afterExecute, which may
     * also throw an exception, which will also cause thread to
     * die. According to JLS Sec 14.20, this exception is the one that
     * will be in effect even if task.run throws.
     *
     * The net effect of the exception mechanics is that afterExecute
     * and the thread's UncaughtExceptionHandler have as accurate
     * information as we can provide about any problems encountered by
     * user code.
     *
     * @param w the worker
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts 允许中断
        boolean completedAbruptly = true;
        try {
			// 不断从等待队列blockingQueue中获取任务
			// 之前addWorker(null, false)这样的线程执行时，会通过getTask中再次获取任务并执行
            while (task != null || (task = getTask()) != null) {
                w.lock(); // 上锁，并不是防止并发执行任务，而是为了防止shutdown()被调用时不终止正在运行的worker线程
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
					// task.run()执行前，由子类实现
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run(); // 执行线程Runable的run方法
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
						// task.run()执行后，由子类实现
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

2、小结：
	• addWorker通过调用t.start()启动了线程，线程池的真正核心执行任务的地方就在此runWorker中；
	• 不断的执行我们提交任务的run方法，可能是刚刚提交的任务，可能是队列中等待的队列，原因在于Worker工作线程类继承了AQS类；
	• Worker重写了AQS的tryAcquire方法，不管先来后到，一种非公平的竞争机制，通过CAS获取锁，获取到了就执行代码块，没获取到的话则添加到CLH队列中通过利用LockSuporrt的park/unpark阻塞任务等待；
	• addWorker通过调用t.start()启动了线程，线程池的真正核心执行任务的地方就在此runWorker中；
```


### 3.processWorkerExit

``` 
1、processWorkerExit源码：
    /**
     * Performs cleanup and bookkeeping for a dying worker. Called
     * only from worker threads. Unless completedAbruptly is set,
     * assumes that workerCount has already been adjusted to account
     * for exit.  This method removes thread from worker set, and
     * possibly terminates the pool or replaces the worker if either
     * it exited due to user task exception or if fewer than
     * corePoolSize workers are running or queue is non-empty but
     * there are no workers.
     *
     * @param w the worker
     * @param completedAbruptly if the worker died due to user exception
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
		// 如果突然中止，说明runWorker中遇到什么异常了，那么正在工作的线程自然就需要减1操作了
        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
		// 执行到此，说明runWorker正常执行完了，需要正常退出工作线程，上锁正常操作移除线程
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks; // 增加线程池完成任务数
            workers.remove(w); // 从workers线程集合中移除已经工作完的线程
        } finally {
            mainLock.unlock();
        }

		// 在对线程池有负效益的操作时，都需要“尝试终止”线程池，主要是判断线程池是否满足终止的状态；
		// 如果状态满足，但还有线程池还有线程，尝试对其发出中断响应，使其能进入退出流程；
		// 没有线程了，更新状态为tidying->terminated；
        tryTerminate();

        int c = ctl.get();
		
		// 如果状态是running、shutdown，即tryTerminate()没有成功终止线程池，尝试再添加一个worker
        if (runStateLessThan(c, STOP)) {
			// 不是突然完成的，即没有task任务可以获取而完成的，计算min，并根据当前worker数量判断是否需要addWorker()
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
				
				// 如果min为0，且workQueue不为空，至少保持一个线程
                if (min == 0 && ! workQueue.isEmpty())
                    min = 1;
					
				// 如果线程数量大于最少数量，直接返回，否则下面至少要addWorker一个
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
			
			// 只要worker是completedAbruptly突然终止的，或者线程数量小于要维护的数量，就新添一个worker线程，即使是shutdown状态
            addWorker(null, false);
        }
    }

2、小结：
	• 异常中止情况worker数量减1，正常情况就上锁从workers中移除；
	• tryTerminate()：在对线程池有负效益的操作时，都需要“尝试终止”线程池;
	• 是否需要增加worker线程，如果线程池还没有完全终止，仍需要保持一定数量的线程;	
``` 


## 四、一些建议
### 4.1、合理配置线程池的大小(仅供参考)
``` 
1、如果是CPU密集型任务，就需要尽量压榨CPU，参考值可以设为 NCPU+1；
2、如果是IO密集型任务，参考值可以设置为2*NCPU；
``` 


### 4.2、JDK帮助文档建议
``` 
“强烈建议程序员使用较为方便的Executors工厂方法：
``` 

## 五、下载地址

[https://gitee.com/ylimhhmily/SpringCloudTutorial.git](https://gitee.com/ylimhhmily/SpringCloudTutorial.git)

SpringCloudTutorial交流QQ群: 235322432

SpringCloudTutorial交流微信群: [微信沟通群二维码图片链接](https://gitee.com/ylimhhmily/SpringCloudTutorial/blob/master/doc/qrcode/SpringCloudWeixinQrcode.png)

欢迎关注，您的肯定是对我最大的支持!!!