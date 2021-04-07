import java.util.ArrayList;

public class Info {
    public String ip;
    public int port;
    public String username;
    public ArrayList<String> Shared_directory;
    public int count_downloads;
    public int count_failures;


    public Info(String ip, int port, String username, ArrayList<String> shared_directory) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        Shared_directory = shared_directory;
        this.count_downloads = 0;
        this.count_failures = 0;
    }
}
