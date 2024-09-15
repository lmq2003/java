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