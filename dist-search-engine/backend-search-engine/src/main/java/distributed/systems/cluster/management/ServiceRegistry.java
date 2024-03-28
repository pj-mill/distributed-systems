package distributed.systems.cluster.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {

    private final ZooKeeper zooKeeper;
    private List<String> allServiceAddresses = null;
    private String currentZnode = null;
    private final String serviceRegistryZnode;

    private static final Logger logger = LogManager.getLogger(ServiceRegistry.class);

    public ServiceRegistry(ZooKeeper zooKeeper, String serviceRegistryZnode) {
        this.zooKeeper = zooKeeper;
        this.serviceRegistryZnode = serviceRegistryZnode;
        createServiceRegistryNode();
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        if (currentZnode != null) {
            logger.info("Already registered to service registry");
            return;
        }
        this.currentZnode = zooKeeper.create(serviceRegistryZnode + "/n_", metadata.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                logger.info("Registered to service registry");
    }

    public void registerForUpdates() {
        try {
            updateAddresses();
        } 
        catch (KeeperException e) {
            logger.error(e);
        } 
        catch (InterruptedException e) {
            logger.error(e);
        }
    }

    public void unregisterFromCluster() {
        try {
            if (currentZnode != null && zooKeeper.exists(currentZnode, false) != null) {
                zooKeeper.delete(currentZnode, -1);
            }
        } 
        catch (KeeperException e) {
            logger.error(e);
        } 
        catch (InterruptedException e) {
            logger.error(e);
        }
    }

    private void createServiceRegistryNode() {
        try {
            if (zooKeeper.exists(serviceRegistryZnode, false) == null) {
                zooKeeper.create(serviceRegistryZnode, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        } 
        catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<String> getAllServiceAddresses() throws KeeperException, InterruptedException {
        if (allServiceAddresses == null) {
            updateAddresses();
        }
        return allServiceAddresses;
    }

    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workers = zooKeeper.getChildren(serviceRegistryZnode, this);

        List<String> addresses = new ArrayList<>(workers.size());

        for (String worker : workers) {
            String serviceFullpath = serviceRegistryZnode + "/" + worker;
            Stat stat = zooKeeper.exists(serviceFullpath, false);
            if (stat == null) {
                continue;
            }

            byte[] addressBytes = zooKeeper.getData(serviceFullpath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }

        this.allServiceAddresses = Collections.unmodifiableList(addresses);
        logger.info("The cluster addresses are: " + this.allServiceAddresses);
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            updateAddresses();
        } 
        catch (KeeperException e) {
            logger.error(e);
        } 
        catch (InterruptedException e) {
            logger.error(e);
        }
    }
}