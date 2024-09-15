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
