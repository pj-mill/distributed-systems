package distributed.systems;

public class Constants {
    public static final String RESOURCES_LOCATION = "C:\\Devs\\Courses New\\distributed-systems\\dist-search-engine\\resources\\books";
    public static final String WORKERS_REGISTRY_ZNODE = "/workers_service_registry";
    public static final String COORDINATORS_REGISTRY_ZNODE = "/coordinators_service_registry";
    public static final String ELECTION_NAMESPACE = "/election";
    public static final String STATUS_ENDPOINT = "/status";
    public static final String SEARCH_WORKER_ENDPOINT = "/task";
    public static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final int SESSION_TIMEOUT = 3000;
}
