import java.util.ArrayList;

public class Info {
    public String ip;
    public int port;
    public String username;
    public int tokenId;
    public ArrayList<String> Shared_directory;
    public int count_downloads;
    public int count_failures;


    public Info(String username) {
        this.username = username;
        this.count_downloads = 0;
        this.count_failures = 0;
    }
}
