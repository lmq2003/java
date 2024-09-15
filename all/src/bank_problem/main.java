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

