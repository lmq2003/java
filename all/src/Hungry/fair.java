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
