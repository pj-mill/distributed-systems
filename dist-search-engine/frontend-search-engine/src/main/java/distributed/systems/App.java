package distributed.systems;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import distributed.systems.cluster.management.ServiceRegistry;
import distributed.systems.networking.WebServer;
import distributed.systems.search.UserSearchHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;

    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        int currentServerPort = 9000;
        if (args.length == 1) {
            currentServerPort = Integer.parseInt(args[0]);
        }

        App application = new App();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry coordinatorsServiceRegistry = new ServiceRegistry(zooKeeper,
                ServiceRegistry.COORDINATORS_REGISTRY_ZNODE);

        UserSearchHandler searchHandler = new UserSearchHandler(coordinatorsServiceRegistry);
        WebServer webServer = new WebServer(currentServerPort, searchHandler);
        webServer.startServer();

        logger.info("Server is listening on port " + currentServerPort);

        application.run();
        application.close();
        logger.info("Shutting down server");
    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    logger.info("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        logger.error("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
