package distributed.systems.cluster.management;

public interface OnElectionCallback {
    void onElectedToBeLeader();
    void onWorker();
}