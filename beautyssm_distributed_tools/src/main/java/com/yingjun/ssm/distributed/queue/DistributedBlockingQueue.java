package com.yingjun.ssm.distributed.queue;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 分布式先进先出阻塞队列
 *
 * @author yingjun
 */
public class DistributedBlockingQueue {
    private final Logger logger = LoggerFactory.getLogger(DistributedBlockingQueue.class);
    private final int ZK_SESSION_TIMEOUT = 5000;
    private String root = "/queue-";
    private CountDownLatch countDownLatch;

    private ZooKeeper zooKeeper;

    public DistributedBlockingQueue(String address, String queueName) {
        if (StringUtils.isBlank(address)) {
            throw new RuntimeException("zookeeper address can not be empty!");
        }
        if (StringUtils.isBlank(queueName)) {
            throw new RuntimeException("queueName can not be empty!");
        }
        zooKeeper = connectServer(address);
        if (zooKeeper != null) {
            root += queueName;
            try {
                Stat stat = zooKeeper.exists(root, false);
                if (stat == null) {
                    zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


    public boolean put(byte[] data) throws KeeperException, InterruptedException {
        logger.info(Thread.currentThread().getName() + "put!!!");
        zooKeeper.create(root + "/queue_", data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        return true;
    }


    public byte[] tack() throws KeeperException, InterruptedException {
        while (true) {
            countDownLatch = new CountDownLatch(1);
            List<String> list = zooKeeper.getChildren(root, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    countDownLatch.countDown();
                }
            });
            if (list.size() == 0) {
                countDownLatch.await();
            } else {
                String[] nodes = list.toArray(new String[list.size()]);
                Arrays.sort(nodes);//从小到大排序
                for (String node : nodes) {
                    try {
                        byte[] data = zooKeeper.getData(root + "/" + node, false, null);
                        zooKeeper.delete(root + "/" + node, -1);
                        return data;
                    } catch (KeeperException.NoNodeException e) {
                        // Another client deleted the node first.
                    }
                }
            }
        }
    }

    /**
     * 连接zookeeper服务器
     *
     * @param address
     * @return
     */
    private ZooKeeper connectServer(String address) {
        final CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(address, ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException e) {
            logger.error("IOException", e);
        } catch (InterruptedException ex) {
            logger.error("InterruptedException", ex);
        }
        return zk;
    }
}
