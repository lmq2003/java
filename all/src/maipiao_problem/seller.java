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
