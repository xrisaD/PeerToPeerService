import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    ArrayList<String> shared_directory; //hash?

    // details
    public String fileName;

    // notify
    public String peerName;







}