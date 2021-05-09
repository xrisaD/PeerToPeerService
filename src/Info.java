import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Info implements Serializable {
    public String ip;
    public int port;
    public String username;
    public int tokenId;
    // only files which peers are seeders
    public ArrayList<String> sharedDirectory;
    // Pieces of a file
    public HashMap<String, ArrayList<String>> pieces;
    // Files that I am seeder
    public HashMap<String, Boolean> seederBit;
    public int countDownloads;
    public int countFailures;


    public Info(String username) {
        this.username = username;
        this.countDownloads = 0;
        this.countFailures = 0;
    }
}
