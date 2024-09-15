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


