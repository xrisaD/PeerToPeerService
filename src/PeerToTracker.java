import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

class PeerToTracker implements Serializable{
    public Method method;
    public StatusCode statusCode;

    // register, login
    public String username;
    public String password;


    // login, logout
    public int token_id;

    // login
    public String ip;
    public int port;
    ArrayList<String> sharedDirectory; //hash?
    HashMap<String, ArrayList<Integer>> pieces;
    HashMap<String, Boolean> seederBit;

    // details
    public String fileName;

    // notify
    public String peerUsername;

    // notify part
    public int id;

    @Override
    public String toString() {
        return "PeerToTracker{" +
                "method=" + method +
                ", statusCode=" + statusCode +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", token_id=" + token_id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", shared_directory=" + sharedDirectory +
                ", fileName='" + fileName + '\'' +
                ", peerUsername='" + peerUsername + '\'' +
                '}';
    }
}