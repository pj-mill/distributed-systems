package distributed.systems.cluster.management;

import org.apache.zookeeper.KeeperException;

import distributed.systems.networking.WebServer;
import distributed.systems.search.SearchWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry serviceRegistry;
    private final int port;
    private WebServer webServer;

    private static final Logger logger = LogManager.getLogger(OnElectionAction.class);

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        serviceRegistry.unregisterFromCluster();
        serviceRegistry.registerForUpdates();
    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        if (webServer == null) {
            webServer = new WebServer(port, searchWorker);
            webServer.startServer();
        }

        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchWorker.getEndpoint());

            serviceRegistry.registerToCluster(currentServerAddress);
        } 
        catch (InterruptedException | UnknownHostException | KeeperException e) {
            logger.error(e);
            return;
        }

    }
}
