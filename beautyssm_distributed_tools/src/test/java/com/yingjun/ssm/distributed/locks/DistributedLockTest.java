package com.yingjun.ssm.distributed.locks;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yingjun
 */
public class DistributedLockTest {

    private final Logger logger = LoggerFactory.getLogger(DistributedLockTest.class);

    @Test
    public void test() throws InterruptedException {
        //用多线程模拟分布式服务
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DistributedLock lock = new DistributedLock("120.27.xxx.xx:2181", "test");
                    try {
                        lock.lock();
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info(Thread.currentThread().getName() + " 开始执行操作!!!");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info(Thread.currentThread().getName() + " 执行结束!!!" );
                    lock.unlock();
                }
            }).start();
        }
        while(true){

        }
    }


}


