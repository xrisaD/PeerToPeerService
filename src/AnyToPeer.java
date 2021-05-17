import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

// AnytoPeer: messages between peers and tracker to peer
public class AnyToPeer implements Serializable {
    public Method method;
    public StatusCode statusCode;
    public Info myInfo;
    public int id;
    byte[] buffer = null;
    public int tokenΙd;
    ArrayList<String> allFiles;
    ArrayList<Info> peerInfo;
    String fileName;


    // ALL_PEERS
    public ConcurrentHashMap<String, Info> usernameToInfo;

    @Override
    public String toString() {
        return "AnyToPeer{" +
                "method=" + method +
                ", statusCode=" + statusCode +
                ", myInfo=" + myInfo +
                ", id=" + id +
                ", tokenΙd=" + tokenΙd +
                ", allFiles=" + allFiles +
                ", peerInfo=" + peerInfo +
                ", fileName='" + fileName + '\'' +
                ", usernameToInfo=" + usernameToInfo +
                '}';
    }
}
