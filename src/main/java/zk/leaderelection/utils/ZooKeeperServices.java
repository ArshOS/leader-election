package zk.leaderelection.utils;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import zk.leaderelection.nodes.ProcessNode;

public class ZooKeeperServices {
    private ZooKeeper zooKeeper;

    public ZooKeeperServices(final String url, final ProcessNode.ProcessNodeWatcher processNodeWatcher) throws IOException {
        zooKeeper = new ZooKeeper(url, 3000, processNodeWatcher);
    }

    public String createNode(final String node, final boolean watch, final boolean ephimeral) {
        String createdNodePath = null;
        try {

            final Stat nodeStat =  zooKeeper.exists(node, watch);

            if(nodeStat == null) {
                createdNodePath = zooKeeper.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE, (ephimeral ?  CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
            } else {
                createdNodePath = node;
            }

        } catch (KeeperException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return createdNodePath;
    }

    public boolean watchNode(final String node, final boolean watch) {

        boolean watched = false;
        try {
            final Stat nodeStat =  zooKeeper.exists(node, watch);

            if(nodeStat != null) {
                watched = true;
            }

        } catch (KeeperException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return watched;
    }

    public List<String> getChildren(final String node, final boolean watch) {

        List<String> childNodes = null;

        try {
            childNodes = zooKeeper.getChildren(node, watch);
        } catch (KeeperException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return childNodes;
    }
}
