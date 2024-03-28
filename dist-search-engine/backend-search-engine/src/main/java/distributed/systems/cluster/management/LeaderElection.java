package distributed.systems.cluster.management;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import distributed.systems.Constants;

import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private String currentZnodeName;
    private final ZooKeeper zooKeeper;
    private OnElectionCallback onElectionCallback;

    private static final Logger logger = LogManager.getLogger(ServiceRegistry.class);

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String znodePrefix = Constants.ELECTION_NAMESPACE + "/c_";
        String znodeFullPath = zooKeeper.create(znodePrefix,
                new byte[]{},
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);

        logger.info("znode name " + znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(Constants.ELECTION_NAMESPACE + "/", "");
    }

    public void reelectLeader() throws KeeperException, InterruptedException {
        Stat predecessorStat = null;
        String predecessorZnodeName = "";

        while (predecessorStat == null) {
            List<String> children = zooKeeper.getChildren(Constants.ELECTION_NAMESPACE, false);

            Collections.sort(children);
            String smallestChild = children.get(0);

            if (smallestChild.equals(currentZnodeName)) {
                logger.info("I am the leader");
                onElectionCallback.onElectedToBeLeader();
                return;
            } 
            else {
                logger.info("I am not the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(Constants.ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }

        onElectionCallback.onWorker();

        logger.info("Watching znode " + predecessorZnodeName);
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case NodeDeleted:
                try {
                    reelectLeader();
                } 
                catch (InterruptedException e) {
                    logger.error(e);
                } 
                catch (KeeperException e) {
                    logger.error(e);
                }
        }
    }
}