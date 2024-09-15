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
