import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class PeerToAny implements Serializable{
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
    ArrayList<String> shared_directory; //hash?

    // details
    public String fileName;

    // notify
    public String peerName;

    // Bytes to sent the file
    byte[] buffer = new byte[4096];
}
