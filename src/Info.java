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
    // Pieces of a file, fileName->pieces
    public HashMap<String, ArrayList<Integer>> pieces;
    // Files that I am seeder, fileName->seederBit for this file
    public HashMap<String, Boolean> seederBit;
    public int countDownloads;
    public int countFailures;

    // constructor for tracker
    public Info(String username) {
        this.username = username;
        this.countDownloads = 0;
        this.countFailures = 0;

    }

    // constructor for peer
    public Info(String username, String ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.countDownloads = 0;
        this.countFailures = 0;

    }
}
