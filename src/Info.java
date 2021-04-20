import java.io.Serializable;
import java.util.ArrayList;

public class Info implements Serializable {
    public String ip;
    public int port;
    public String username;
    public int tokenId;
    public ArrayList<String> sharedDirectory;
    public int countDownloads;
    public int countFailures;


    public Info(String username) {
        this.username = username;
        this.countDownloads = 0;
        this.countFailures = 0;
    }
}
