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
