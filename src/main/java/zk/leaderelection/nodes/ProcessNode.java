package zk.leaderelection.nodes;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import zk.leaderelection.utils.ZooKeeperServices;

public class ProcessNode implements Runnable{

    private static final Logger LOG = Logger.getLogger(ProcessNode.class);

    private static final String LEADER_ELECTION_ROOT_NODE = "/election";
    private static final String PROCESS_NODE_PREFIX = "/p_";

    private final int id;
    private final ZooKeeperServices zooKeeperService;

    private String processNodePath;
    private String watchedNodePath;


    public ProcessNode(final int id, final String zkURL) throws IOException {
        this.id = id;
        zooKeeperService = new ZooKeeperServices(zkURL, new ProcessNodeWatcher());
    }

    private void attemptForLeaderPosition() {

        final List<String> childNodePaths = zooKeeperService.getChildren(LEADER_ELECTION_ROOT_NODE, false);

        Collections.sort(childNodePaths);

        int index = childNodePaths.indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));
        if(index == 0) {
            if(LOG.isInfoEnabled()) {
                LOG.info("[Process: " + id + "] I am the new leader!");
            }
        } else {
            final String watchedNodeShortPath = childNodePaths.get(index - 1);

            watchedNodePath = LEADER_ELECTION_ROOT_NODE + "/" + watchedNodeShortPath;

            if(LOG.isInfoEnabled()) {
                LOG.info("[Process: " + id + "] - Setting watch on node with path: " + watchedNodePath);
            }
            zooKeeperService.watchNode(watchedNodePath, true);
        }
    }

    @Override
    public void run() {

        if(LOG.isInfoEnabled()) {
            LOG.info("Process with id: " + id + " has started!");
        }

        final String rootNodePath = zooKeeperService.createNode(LEADER_ELECTION_ROOT_NODE, false, false);
        if(rootNodePath == null) {
            throw new IllegalStateException("Unable to create/access leader election root node with path: " + LEADER_ELECTION_ROOT_NODE);
        }

        processNodePath = zooKeeperService.createNode(rootNodePath + PROCESS_NODE_PREFIX, false, true);
        if(processNodePath == null) {
            throw new IllegalStateException("Unable to create/access process node with path: " + LEADER_ELECTION_ROOT_NODE);
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("[Process: " + id + "] Process node created with path: " + processNodePath);
        }

        attemptForLeaderPosition();
    }

    public class ProcessNodeWatcher implements Watcher{

        @Override
        public void process(WatchedEvent event) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("[Process: " + id + "] Event received: " + event);
            }

            final EventType eventType = event.getType();
            if(EventType.NodeDeleted.equals(eventType)) {
                if(event.getPath().equalsIgnoreCase(watchedNodePath)) {
                    attemptForLeaderPosition();
                }
            }
        }
    }
}
