import java.io.IOException;
import org.apache.zookeeper.KeeperException;

public class Program {
    
    public static void main(String[] arg) throws IOException, KeeperException, InterruptedException  {
        
        // new LeaderElection();

        // new Watchers();

        new LeaderReelection();
        
    }
}
