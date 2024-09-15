# 一 Java安全问题复现及解决



## 1 数据竞争（Race Condition）

### （1）问题复现

**问题描述：** 当多个线程同时修改共享变量，可能会导致数据的不一致性。对于此问题，对生活中买票以及银行取钱这两种行为进行模拟。

#### **1）买票**

```java
package maipiao_problem;

public class seller extends Thread{
    private static int num=5;

    @Override
    public void run() {
        while(true){
            num--;
            System.out.println("还剩"+num+"张票");
            if(num==0 || num<0){
                System.out.println("售票结束");
                break;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}

package maipiao_problem;

public class main {
    public static void main(String[] args) {

        seller a = new seller();
        seller b = new seller();
        seller c = new seller();
        a.start();
        b.start();
        c.start();

    }
}

```

#### **2）银行取钱**

```java
package bank_problem;

/**
 定义账户类
 */
  class Account {
    private double money; // 账户的余额

    public Account(double money) {
        this.money = money;
    }

    public Account() {
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    /**
     * 取钱功能
     * @param money:取钱的金额
     */
    public void drawMoney(double money){
        // 1.先获取是谁来取钱,线程的名字设置的是人名
        String name = Thread.currentThread().getName();
        // 2.判单账户的余额 >= 取钱的金额
        if(this.money >= money){
            // 可以取钱了
            System.out.println(name + "取钱成功,取出" + money + "元!");
//            setMoney(getMoney() - money);
            // 更新余额
            this.money -= money;
            System.out.println(name + "取钱后共享账户剩余:" + this.money);
        }else{
            System.out.println(name + "来取钱,账户余额不足");
        }
    }
}

/**
 取钱的线程类
 */
 class DrawThread extends Thread {
    // 接收处理的账户对象
    private Account acc;

    /**
     * 有参构造器
     * @param acc:接共享的账户对象
     * @param name:线程名
     */
    public DrawThread(Account acc,String name) {
        super(name);
        this.acc = acc;
    }

    public DrawThread() {
    }

    public Account getAcc() {
        return acc;
    }

    public void setAcc(Account acc) {
        this.acc = acc;
    }
    @Override
    public void run() {
        // 小明、小红:取钱
        acc.drawMoney(100000);
    }
}


package bank_problem;
/*
小红与小明是一对夫妻，他们有一个共同账户，存款10万元，模拟他们同时去取钱

 */



/**
 需求:模拟取钱案例
 */
public class main {
    public static void main(String[] args) throws InterruptedException {
        // 1.定义账户类,创建一个账户对象代表2个人共享的账户对象
        Account acc = new Account(100000);

        // 2.定义线程类,创建两个线程对象,代表小明和小红同时进来了
        // 直接new对象这叫匿名对象
        new DrawThread(acc,"小明").start();
//        DrawThread.sleep(30);
        new DrawThread(acc,"小红").start();
    }
}


```





### （2）问题的解决

**解决方案：**使用synchronized关键字来实现同步代码块或者同步锁。

#### **1）卖票**

```java
package maipiao_synch;

/*
使用同步代码块，在限制访问的同时，保持run方法的并发性
*/

 class seller_sy extends Thread {
     private static int num = 5;
     private static final Object lock = new Object(); // 创建一个锁对象

     @Override
     public void run() {
         while (true) {
             synchronized (lock) { // 同步代码块
                 if (num > 0) {
                     System.out.println("有" + num + "张票");
                     num--;
                     System.out.println("还剩" + num + "张票");
                 } else {
                     System.out.println("售票结束");
                     break;
                 }
             }

             try {
                 Thread.sleep(2000);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
     }

 }

 /*
 限制了方法的并发性
 public class Seller extends Thread {
    private static int num = 5;

    @Override
    public void run() {
        synchronized(this) { // 也可以同步当前类的Class对象：Seller.class
            while (true) {
                num--;
                System.out.println("还剩" + num + "张票");
                if (num == 0 || num < 0) {
                    System.out.println("售票结束");
                    break;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
  */
```





#### **2）银行取钱**

```java
package bank_synch;


/**
 定义账户类
 */
 class Account {
    private double money; // 账户的余额

    public Account(double money) {
        this.money = money;
    }

    public Account() {
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    /**
     * 取钱功能
     * @param money:取钱的金额
     */
    public void drawMoney(double money){
        // 1.先获取是谁来取钱,线程的名字设置的是人名
        String name = Thread.currentThread().getName();
        // 同步代码块
        // 小明,小红  唯一的同步锁对象
        // 规范上,建议使用共享资源作为锁对象   this = acc 共享账户
        // 对于实例方法建议使用this作为锁对象
        synchronized (this) {  //  acc.drawMoney(100000);
            // 2.判单账户的余额 >= 取钱的金额
            if(this.money >= money){
                // 可以取钱了
                System.out.println(name + "取钱成功,取出" + money + "元!");
                //            setMoney(getMoney() - money);
                // 更新余额
                this.money -= money;
                System.out.println(name + "取钱后共享账户剩余:" + this.money);
            }else{
                System.out.println(name + "来取钱,账户余额不足");
            }
        }
    }
}

 /**
  取钱的线程类
  */
  class DrawThread extends Thread {
     // 接收处理的账户对象
     private Account acc;

     /**
      * 有参构造器
      * @param acc:接共享的账户对象
      * @param name:线程名
      */
     public DrawThread(Account acc, String name) {
         super(name);
         this.acc = acc;
     }

     public DrawThread() {
     }

     public Account getAcc() {
         return acc;
     }

     public void setAcc(Account acc) {
         this.acc = acc;
     }
     @Override
     public void run() {
         // 小明、小红:取钱
         acc.drawMoney(100000);
     }
 }

package bank_synch;



/**
 需求:模拟取钱案例
 */
public class main {
    public static void main(String[] args) throws InterruptedException {
        // 测试线程安全问题
        // 1.定义账户类,创建一个账户对象代表2个人共享的账户对象
        Account acc = new Account(100000);

        // 2.定义线程类,创建两个线程对象,代表小明和小红同时进来了
        // 直接new对象这叫匿名对象
        new DrawThread(acc,"小明").start();
//        DrawThread.sleep(30);
        new DrawThread(acc,"小红").start();
    }
}
```

## 2 线程饥饿（Thread Starvation）

### （1）问题复现

**问题描述：**某些线程可能长时间得不到执行的机会，导致线程无法正常运行。

```java
package Hungry;

public class hungry {
    public static void main(String[] args) {
        // 创建多个线程，其中一个高优先级线程和多个低优先级线程
        Thread highPriorityThread = new Thread(new WorkerTask(), "高优先级线程");
        Thread lowPriorityThread1 = new Thread(new WorkerTask(), "低优先级线程1");
        Thread lowPriorityThread2 = new Thread(new WorkerTask(), "低优先级线程2");
        Thread lowPriorityThread3 = new Thread(new WorkerTask(), "低优先级线程3");

        // 设置线程优先级，高优先级线程设置为最大值
        highPriorityThread.setPriority(Thread.MAX_PRIORITY);
        lowPriorityThread1.setPriority(Thread.MIN_PRIORITY);
        lowPriorityThread2.setPriority(Thread.MIN_PRIORITY);
        lowPriorityThread3.setPriority(Thread.MIN_PRIORITY);

        // 启动所有线程
        lowPriorityThread1.start();
        lowPriorityThread2.start();
        lowPriorityThread3.start();
        highPriorityThread.start();
    }
}

class WorkerTask implements Runnable {
    @Override
    public void run() {
        // 模拟耗时任务
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " 正在执行任务 " + i);
            try {
                // 通过sleep模拟执行过程中的等待
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

```

### （2）问题的解决

**解决方案：**使用公平锁Reentrant-Lock的公平模式来避免线程饥饿问题。

```java
package Hungry;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class fair {
    public static void main(String[] args) {
        FairWorkerTask task = new FairWorkerTask();

        Thread thread1 = new Thread(task, "线程1");
        Thread thread2 = new Thread(task, "线程2");
        Thread thread3 = new Thread(task, "线程3");
        Thread thread4 = new Thread(task, "线程4");

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }
}

class FairWorkerTask implements Runnable {
    // 使用公平锁
    private final Lock lock = new ReentrantLock(true);

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 正在执行任务 " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                lock.unlock();
            }
        }
    }
}

```

## 3 死锁（Deadlock）

### （1）问题复现

**问题描述：** 当多个线程同时修改共享变量，可能会导致数据的不一致性。

```java
package DeadLock;


public class LockTest {
    public static String obj1 = "obj1";
    public static String obj2 = "obj2";
    public static void main(String[] args) {
        LockA la = new LockA(); //锁A
        new Thread(la).start();
        LockB lb = new LockB(); //锁B
        new Thread(lb).start();
    }
}
class LockA implements Runnable{
    public void run() {
        try { //异常捕获
            System.out.println(" LockA 开始执行");
            while(true){
                synchronized (LockTest.obj1) {
                    System.out.println(" LockA 锁住 obj1");
                    Thread.sleep(3000); // 此处等待是给B能锁住机会
                    synchronized (LockTest.obj2) {
                        System.out.println(" LockA 锁住 obj2");
                        Thread.sleep(60 * 1000); //占据资源
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class LockB implements Runnable{
    public void run() {
        try {
            System.out.println(" LockB 开始执行");
            while(true){
                synchronized (LockTest.obj2) {
                    System.out.println(" LockB 锁住 obj2");
                    Thread.sleep(3000); //给A能锁住机会
                    synchronized (LockTest.obj1) {
                        System.out.println(" LockB 锁住 obj1");
                        Thread.sleep(60 * 1000); //占据资源
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



### （2）问题的解决

**解决方法：** 使用适当的同步机制来协调线程之间的状态变化。

```java
package DeadLock;

/*
不使用锁，使用信号量控制


 */

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
public class UnLockTest {
    public static String obj1 = "obj1";
    public static final Semaphore a1 = new Semaphore(1);
    public static String obj2 = "obj2";
    public static final Semaphore a2 = new Semaphore(1);
    public static void main(String[] args) {
        LockAa la = new LockAa();
        new Thread(la).start();
        LockBb lb = new LockBb();
        new Thread(lb).start();
    }
}
class LockAa implements Runnable {
    public void run() {
        try { //捕获异常
            System.out.println(" LockA 开始执行");
            while (true) {
                if (UnLockTest.a1.tryAcquire(1, TimeUnit.SECONDS)) {
                    System.out.println(" LockA 锁住 obj1");
                    if (UnLockTest.a2.tryAcquire(1, TimeUnit.SECONDS)) {
                        System.out.println(" LockA 锁住 obj2");
                        Thread.sleep(60 * 1000); // do something
                    }else{
                        System.out.println("LockA 锁 obj2 失败");
                    }
                }else{
                    System.out.println("LockA 锁 obj1 失败");
                }
                UnLockTest.a1.release(); // 释放
                UnLockTest.a2.release();
                Thread.sleep(1000); // 马上进行尝试，现实情况下do something是不确定的
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class LockBb implements Runnable {
    public void run() {
        try { //捕获异常
            System.out.println(" LockB 开始执行");
            while (true) {
                if (UnLockTest.a2.tryAcquire(1, TimeUnit.SECONDS)) {
                    System.out.println(" LockB 锁住 obj2");
                    if (UnLockTest.a1.tryAcquire(1, TimeUnit.SECONDS)) {
                        System.out.println(" LockB 锁住 obj1");
                        Thread.sleep(60 * 1000); // do something
                    }else{
                        System.out.println("LockB 锁 obj1 失败");
                    }
                }else{
                    System.out.println("LockB 锁 obj2 失败");
                }
                UnLockTest.a1.release(); // 释放
                UnLockTest.a2.release();
                Thread.sleep(10 * 1000); //tryAcquire只用1秒，而且B要给A让出能执行的时间，否则两个永远是死锁
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```



## 4活锁（Livelock）

### （1）问题复现

**问题概述：**线程状态变化不断但没有实际进展的情况。线程通过不断调整自身状态来响应对方，但无法完成任务。

```java
package LiveLock;

class Worker {
    private final String name;
    private boolean active;

    public Worker(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void work(SharedResource resource, Worker otherWorker) {
        while (active) {
            // 如果对方工人也在尝试工作，就礼让
            if (otherWorker.isActive()) {
                System.out.println(name + ": " + otherWorker.getName() + " 正在工作，等待中...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            // 取得资源并工作
            System.out.println(name + ": 正在工作...");
            resource.use();
            active = false;
        }
    }
}

class SharedResource {
    public void use() {
        System.out.println("资源正在被使用");
    }
}

public class LivelockExample {
    public static void main(String[] args) {
        final Worker worker1 = new Worker("工人1", true);
        final Worker worker2 = new Worker("工人2", true);
        final SharedResource resource = new SharedResource();

        // 启动两个线程
        new Thread(() -> worker1.work(resource, worker2)).start();
        new Thread(() -> worker2.work(resource, worker1)).start();
    }
}
```

### （2）问题的解决

**解决方案：**

​	      引入随机等待时间或限制重试次数，打破活锁状态

```java
package LiveLock;


import java.util.Random;

class uWorker {
    private final String name;
    private boolean active;
    private final Random random = new Random();

    public uWorker(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void uwork(SharedResource resource, uWorker otherWorker) {
        while (active) {
            if (otherWorker.isActive()) {
                System.out.println(name + ": " + otherWorker.getName() + " 正在工作，等待中...");
                try {
                    // 引入随机等待时间
                    Thread.sleep(random.nextInt(100) + 50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            System.out.println(name + ": 正在工作...");
            resource.use();
            active = false;
        }
    }
}

public class UnLivelock {
    public static void main(String[] args) {
        final uWorker worker1 = new uWorker("工人1", true);
        final uWorker worker2 = new uWorker("工人2", true);
        final SharedResource resource = new SharedResource();

        new Thread(() -> worker1.uwork(resource, worker2)).start();
        new Thread(() -> worker2.uwork(resource, worker1)).start();
    }
}

```

# 二 JVM

## 1 JVM垃圾回收器

**JVM参数：**

- `-Xms`：设置JVM初始堆大小
- `-Xmx`：设置JVM最大堆大小
- `-XX:NewRatio`：设置新生代与老年代的大小比例
- `-XX:SurvivorRatio`：设置Survivor区域的大小比例
- `-XX:MetaspaceSize`：设置Metaspace区域的初始大小
- `-XX:MaxMetaspaceSize`：设置Metaspace区域的最大大小

**垃圾回收相关参数：**

- `-XX:+UseG1GC`：启用G1垃圾收集器
- `-XX:+UseParallelGC`：启用并行垃圾收集器
- `-XX:+UseConcMarkSweepGC`：启用CMS垃圾收集器（CMS 已废弃，G1是推荐的替代方案）
- `-XX:+PrintGCDetails`：输出详细的GC日志信息
- `-XX:+PrintGCDateStamps`：在GC日志中显示日期戳
- `-Xloggc:<file>`：将GC日志写入指定文件
- `-XX:+HeapDumpOnOutOfMemoryError`：在内存溢出时生成堆转储文件
- `-XX:HeapDumpPath=<path`>：指定堆转储文件的路径



**PS：CMS，GC，G1，Parallel详细参数设置见file垃圾回收参数**



## 

**不同垃圾回收器的工作原理**：

- **Serial GC**：单线程垃圾回收
- **Parallel GC**：多线程并行垃圾回收
- **G1 GC**（Garbage First）：面向大内存的低延迟GC算法
- **ZGC**：极低延迟的垃圾回收器（适合大内存场景）
- **Shenandoah GC**：另一种低延迟垃圾回收器

PS：

## 2堆栈信息操作

### 1 Jstack命令

概述：jstack是JVM自带的Java堆栈跟踪工具，它用于打印出给定的java进程ID、core file、远程调试服务的Java堆栈信息.

```javascript
jstack命令格式如下：
jstack [ option ] pid
jstack [ option ] executable core
jstack [ option ] [server-id@]remote-hostname-or-IP
                   
executable Java executable from which the core dump was produced.(可能是产生core dump的java可执行程序)
core 将被打印信息的core dump文件
remote-hostname-or-IP 远程debug服务的主机名或ip
server-id 唯一id,假如一台主机上多个远程debug服务


-F参数
如果Java虚拟机进程由于进程挂起而没有任何响应，那么可以使用-F参数（仅在Oracle Solaris和Linux操作系统上游戏）强制显示线程快照信息。

-l参数
如果使用-l参数，除了方法栈帧以外，jstack命令还会显示关于锁的附加信息，比如属于java.util.concurrent的ownable synchronizers列表。

-m参数
如果使用-m参数，jstack命令将显示混合的栈帧信息，除了Java方法栈帧以外，还有本地方法栈帧。本地方法栈帧是C或C++编写的虚拟机代码或JNI/native代码。
```



### 2 Jmap命令

概述：jstack是JVM自带的Java堆栈跟踪工具，它用于打印出给定的java进程ID、core file、远程调试服务的Java堆栈信息.

```javascript
jstack命令格式如下：
jstack [ option ] pid
jstack [ option ] executable core
jstack [ option ] [server-id@]remote-hostname-or-IP

executable Java executable from which the core dump was produced.(可能是产生core dump的java可执行程序)
core 将被打印信息的core dump文件
remote-hostname-or-IP 远程debug服务的主机名或ip
server-id 唯一id,假如一台主机上多个远程debug服务


-F参数
如果Java虚拟机进程由于进程挂起而没有任何响应，那么可以使用-F参数（仅在Oracle Solaris和Linux操作系统上游戏）强制显示线程快照信息。

-l参数
如果使用-l参数，除了方法栈帧以外，jstack命令还会显示关于锁的附加信息，比如属于java.util.concurrent的ownable synchronizers列表。

-m参数
如果使用-m参数，jstack命令将显示混合的栈帧信息，除了Java方法栈帧以外，还有本地方法栈帧。本地方法栈帧是C或C++编写的虚拟机代码或JNI/native代码
```



### 3使用工具分析堆转储文件：

**Eclipse MAT（Memory Analyzer Tool）**：用于分析Java内存问题的工具

**VisualVM**：一个可视化的JVM监控工具，可实时查看线程、堆和GC信息



## 3 JVM进程管理

**如何查看和管理JVM进程：**

- `jps`：列出JVM进程ID
- `jstat`：监控JVM垃圾回收、类加载、内存等信息
- `top` 或 `htop`：查看系统进程的资源占用情况
- `ps -ef | grep java`：查找运行的Java进程
- `kill -9 <pid>`：强制终止Java进程

（详情可见官网）

## 4 JVM 垃圾回收信息

### 1 GC

开启简单日志

`-XX:+PrintGC`

详细日志

`-XX:+PrintGCDetails`

时间戳日志

`-XX:+PrintGCTimeStamps`

`XX:+PrintGCDateStamps`

GC路径设置

`-Xloggc:filename` 

滚动日志

`-XX:+UseGCLogFileRotation`

设置文件数量

`-XX:NumberOfGCLogFiles=N`

设置GC文件大小

`-XX:GCLogFileSize=N`

其他参数

```javascript
-XX:+PrintGCDateStamps  打印 gc 发生的时间戳（日期格式）
-XX:+PrintGCTimeStamps  打印 gc 发生的时间戳
-XX:+PrintTenuringDistribution  打印 gc 发生时的分代信息
-XX:+PrintGCApplicationStoppedTime  打印 gc 停顿时长
-XX:+PrintGCApplicationConcurrentTime   打印 gc 间隔的服务运行时长
-XX:+PrintGCDetails 打印 gc 详情，包括 gc 前/内存等
-Xloggc:…/gclogs/gc.log.date    指定 gc log 的路径
-XX:+PrintHeapAtGC  GC 后打印堆数据
-XX:+PrintGCApplicationStoppedTime  打印 STW（服务暂停） 时间
-XX:+PrintReferenceGC   打印 Reference 处理信息（强引用/弱引用/软引用/虚引用/finalize ）
-Xloggc:/path/to/gc-%t.log  使用时间戳命名文件
```

### 2 Serial

```javascript
27.775: [GC (Allocation Failure) 27.775: [ParNew: 153344K->17023K(153344K), 0.0297733 secs] 840919K->716568K(1031552K), 0.0298356 secs] [Times: user=0.05 sys=0.00, real=0.03 secs] 

## 时间戳(基于JVM启动时间开始,为0):[GC (发生GC的原因) [新生代: GC前->GC后(该区总大小)] 堆区GC前->GC后(堆区总大小), GC所用的时间] [Times: 用户态耗费的时间 sys是内核态耗费的时间, real=实际花费的时间] 
16.935: [GC (Allocation Failure) [PSYoungGen: 268064K->64512K(284672K)] 724584K->577312K(984064K), 0.0641592 secs] [Times: user=0.25 sys=0.01, real=0.06 secs] 
......
## 时间戳(基于JVM启动时间开始,为0):[Full GC  (发生GC的原因) [新生代: GC前->GC后(该区总大小)]  [老年代: GC前->GC后(该区总大小)] 堆区GC前->GC后(堆区总大小),[元数据区: GC前->GC后(该区总大小)], GC所用的时间] [Times: 用户态耗费的时间 sys是内核态耗费的时间, real=实际花费的时间] 
17.123: [Full GC (Ergonomics) [PSYoungGen: 64512K->0K(181248K)] [ParOldGen: 592712K->538414K(699392K)] 657224K->538414K(880640K), [Metaspace: 3178K->3178K(1056768K)], 0.5827661 secs] [Times: user=2.32 sys=0.00, real=0.59 secs] 

```

### 3 Parallel

```javascript
## 时间戳(基于JVM启动时间开始,为0):[GC (发生GC的原因) [新生代: GC前->GC后(该区总大小)] 堆区GC前->GC后(堆区总大小), GC所用的时间] [Times: 用户态耗费的时间 sys是内核态耗费的时间, real=实际花费的时间] 
9.987: [GC (Metadata GC Threshold) [PSYoungGen: 82140K->8352K(389632K)] 143035K->69255K(540160K), 0.0052497 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 


## 时间戳(基于JVM启动时间开始,为0):[Full GC  (发生GC的原因) [新生代: GC前->GC后(该区总大小)]  [老年代: GC前->GC后(该区总大小)] 堆区GC前->GC后(堆区总大小),[元数据区: GC前->GC后(该区总大小)], GC所用的时间] [Times: 用户态耗费的时间 sys是内核态耗费的时间, real=实际花费的时间] 
20.228: [Full GC (Metadata GC Threshold) [PSYoungGen: 34406K->0K(673280K)] [ParOldGen: 142960K->143875K(423424K)] 177367K->143875K(1096704K), [Metaspace: 94775K->94775K(1134592K)], 0.1531552 secs] [Times: user=0.92 sys=0.00, real=0.15 secs] 

## 时间戳(基于JVM启动时间开始,为0):[GC (发生GC的原因) [新生代: GC前->GC后(该区总大小)] 堆区GC前->GC后(堆区总大小), GC所用的时间] [Times: 用户态耗费的时间 sys是内核态耗费的时间, real=实际花费的时间] 
64.750: [GC (Allocation Failure) [PSYoungGen: 786432K->8757K(917504K)] 786432K->8838K(3014656K), 0.0064866 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
```

### 4 ConMarkSweep

```javascript
## 此阶段表示CMS已经开始初始标记
## 开始时间: [GC (GC阶段) [1 CMS-initial-mark: 当前老年代大小(老年代可用总空间大小)] 当前使用堆大小(堆总空间大小), 0.0094830 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
3.104: [GC (CMS Initial Mark) [1 CMS-initial-mark: 359454K(707840K)] 398926K(1014528K), 0.0094830 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
## 此时开始并发标记阶段，因为不是暂停的所以中间会穿插YGC
3.114: [CMS-concurrent-mark-start]
## 时间戳(基于JVM启动时间开始,为0):[GC (发生GC的原因) [新生代: GC前->GC后(该区总大小)] 堆区GC前->GC后(堆区总大小), GC所用的时间] [Times: 用户态耗费的时间 sys是内核态耗费的时间, real=实际花费的时间] 
3.161: [GC (Allocation Failure) 3.161: [ParNew: 306688K->8511K(306688K), 0.0314696 secs] 666142K->402013K(1014528K), 0.0315284 secs] [Times: user=0.12 sys=0.00, real=0.04 secs] 
......
## 此时并发标记结束
3.584: [CMS-concurrent-mark: 0.427/0.471 secs] [Times: user=1.00 sys=0.00, real=0.47 secs] 
## 此时预清理阶段开始
3.584: [CMS-concurrent-preclean-start]
3.585: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
## 可中断的预清理开始，因为是可以中断的所以也会穿插YGC
3.585: [CMS-concurrent-abortable-preclean-start]
3.630: [GC (Allocation Failure) 3.630: [ParNew: 272646K->4K(306688K), 0.0010589 secs] 666148K->393506K(1014528K), 0.0011269 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
......
## 可中断的预清理结束
4.103: [CMS-concurrent-abortable-preclean: 0.007/0.518 secs] [Times: user=0.55 sys=0.00, real=0.52 secs] 
## 最终标记开始最终标记
## 开始时间: [GC (GC阶段) [年轻代: 使用量 (总量)]
4.103: [GC (CMS Final Remark) [YG occupancy: 5456 K (306688 K)]

## 开始时间: [标记活动对象耗时 , 0.0015436 secs]
4.103: [Rescan (parallel) , 0.0015436 secs]
## 开始时间: [weak refs processing, 0.0000190 secs]
4.105: [处理弱引用时间, 0.0000190 secs]
## 开始时间:  [类卸载时间, 0.0002399 secs]
4.105: [class unloading, 0.0002399 secs]
## 开始时间:  [清理符号（保存类级元数据）时间, 0.0002682 secs]
4.105: [scrub symbol table, 0.0002682 secs]
## 开始时间:  [清理字符串（内部化字符串）时间, 0.0000932 secs][1 CMS-remark: 老年代使用量 (总量)] 堆使用量(总量), 0.0022104 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
4.105: [scrub string table, 0.0000932 secs][1 CMS-remark: 393502K(707840K)] 398958K(1014528K), 0.0022104 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
## 开始清理阶段清除阶段，会和YGC并行产生
4.106: [CMS-concurrent-sweep-start]
4.156: [GC (Allocation Failure) 4.156: [ParNew: 272644K->4K(306688K), 0.0011464 secs] 666146K->393506K(1014528K), 0.0011886 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
......
4.249: [CMS-concurrent-sweep: 0.139/0.143 secs] [Times: user=0.29 sys=0.00, real=0.14 secs] 
## 重置CMS数据结构开始新一轮并发回收周期
4.249: [CMS-concurrent-reset-start]
4.252: [CMS-concurrent-reset: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
```

### 5 YGC

```javascript
## 开始进行YGC
2.377: [GC pause (G1 Evacuation Pause) (young), 0.0254900 secs]
    ## 清理耗时，并行的GC任务线程数
   [Parallel Time: 24.6 ms, GC Workers: 4]
      ## Min 第一个垃圾收集线程开始工作时 JVM 启动经过的时间
      ## Max 最有一个垃圾收集线程开始工作时 JVM 启动后经过的时间
      ## Diff 启动差值差距过大的时候表示机器上其他的进程正在从 JVM 垃圾收集进程中抢占 CPU 资源
      [GC Worker Start (ms): Min: 2377.6, Avg: 2379.1, Max: 2383.1, Diff: 5.6]
      ## 扫描 root 集合花费的时间
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.2]
      ## 更新RSet
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         ## 表示 Update RS 这个过程中处理了多少个日志缓冲区
         [Processed Buffers: Min: 0, Avg: 0.2, Max: 1, Diff: 1, Sum: 1]
      ## 扫描每个新生代分区的 RSet
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      ## 扫描代码中的 Root 节点花费的时间
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      ## 将当前分区中存活的对象拷贝到新的分区中 时间
      [Object Copy (ms): Min: 18.3, Avg: 22.2, Max: 23.7, Diff: 5.4, Sum: 89.0]
      ## 当一个垃圾收集线程完成任务时，它会进入一个临界区，并尝试帮助其他垃圾线程完成任务，min 表示该垃圾收集线程什么时候尝试Termination
      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         ## 再次尝试 terminate
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 4]
       ## 垃圾收集线程完成其他任务的时间
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      ## 每个垃圾收集线程的最小、最大、平均、差值和总共的时间
      [GC Worker Total (ms): Min: 18.3, Avg: 22.3, Max: 23.8, Diff: 5.6, Sum: 89.2]
      ## 垃圾回收结束时间
      [GC Worker End (ms): Min: 2401.4, Avg: 2401.4, Max: 2401.4, Diff: 0.0]
   ## 释放用于管理并行活动的数据结构
   [Code Root Fixup: 0.0 ms]
   ## 清理更多的数据结构
   [Code Root Purge: 0.0 ms]
   ## 清理 card table
   [Clear CT: 0.2 ms]
   ## 其他活动
   [Other: 0.6 ms]
        ## 选择要进行回收的分区放入 CSet 中
      [Choose CSet: 0.0 ms]
      ## 处理Java 中的 软引用、弱引用、fianl、phantom、JNI 
      [Ref Proc: 0.2 ms]
      ## 遍历所有引用，将不能回收的放入 pending 表
      [Ref Enq: 0.0 ms]
      ## 在回收过程中被修改的 card 将会被重置为“脏卡”
      [Redirty Cards: 0.3 ms]
      ## 大对象可以在新生代收集的时候被回收
      [Humongous Register: 0.0 ms]
      ## 确保巨型对象可以被回收、释放该巨型对象所占的分区、重置分区类型、将分区还给 free 列表、更新空闲空间大小时间
      [Humongous Reclaim: 0.0 ms]
      ## 将要释放的分区还回 free 列表
      [Free CSet: 0.0 ms]
   ## GC前后 Eden使用量(总容量)  survivors 区前后使用量 JVM堆GC前后内存使用量(总容量)
   [Eden: 52224.0K(52224.0K)->0.0B(45056.0K) Survivors: 0.0B->7168.0K Heap: 278.2M(1024.0M)->290.0M(1024.0M)]
 ## 时间  
 [Times: user=0.09 sys=0.00, real=0.03 secs] 
```

### 6 并发GC

```javascript
## 开始进行初始标记
 [Times: user=0.13 sys=0.00, real=0.03 secs] 
15.214: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0079992 secs]
   [Parallel Time: 5.6 ms, GC Workers: 4]
   ......
   [Eden: 324.0M(324.0M)->0.0B(379.0M) Survivors: 51200.0K->9216.0K Heap: 846.7M(1024.0M)->481.7M(1024.0M)]
 [Times: user=0.02 sys=0.00, real=0.01 secs] 
## 开始进行根区域扫描
15.223: [GC concurrent-root-region-scan-start]
15.231: [GC concurrent-root-region-scan-end, 0.0085091 secs]
## 开始进行并发标记
15.231: [GC concurrent-mark-start]
16.257: [GC concurrent-mark-end, 1.0253542 secs]

## 开始进行重新标记（最终标记）
16.257: [GC remark 16.257: [Finalize Marking, 0.0003073 secs] 16.257: [GC ref-proc, 0.0001121 secs] 16.257: [Unloading, 0.0006524 secs], 0.0022019 secs]
 [Times: user=0.00 sys=0.00, real=0.01 secs] 
## 开始进行清理阶段
16.260: [GC cleanup 792M->721M(1024M), 0.0012676 secs]
 [Times: user=0.00 sys=0.00, real=0.00 secs] 
16.261: [GC concurrent-cleanup-start]
16.261: [GC concurrent-cleanup-end, 0.0000275 secs]

```

7 Full GC

```javascript
## Full GC
## GC原因
9.886: [Full GC (Allocation Failure)  511M->289M(512M), 0.7627054 secs]
## Eden、Survivors，堆区，元数据区使用量（总量）变化
   [Eden: 0.0B(25.0M)->0.0B(96.0M) Survivors: 0.0B->0.0B Heap: 511.5M(512.0M)->289.4M(512.0M)], [Metaspace: 2663K->2663K(1056768K)]
 [Times: user=0.86 sys=0.02, real=0.76 secs] 
10.649: [GC concurrent-mark-abort]
```

。
