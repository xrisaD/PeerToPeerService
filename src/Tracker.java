import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<Integer, Info> TokenId_toInfo = new ConcurrentHashMap<Integer, Info>();
    ConcurrentHashMap<String, ArrayList<Integer>> Files_toToken = new ConcurrentHashMap<String, ArrayList<Integer>>();
    ArrayList<Integer> All_tokenids = new ArrayList<Integer>();
    ArrayList<String> All_files = new ArrayList<String>();
    private final String ip;
    private final int port;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Tracker(String ip, int port, String fileDownloadListPath) {
        // 0: ip, 1: port
        // 2: fineDownloadListPath
        this.ip = ip;
        this.port = port;
        All_files = Util.readFileDownloadList(fileDownloadListPath);

        FillFiles_toToken();
        //TODO: Read TXT and fill the All_files array;
    }

    public void startServer() {
        ServerSocket providerSocket = null;
        Socket connection = null;
        try {
            providerSocket = new ServerSocket(this.port, 10);
            System.out.println("Tracker listening on port " + getPort());
            while (true) {
                connection = providerSocket.accept();
                //We start a thread
                //this thread will do the communication
                TrackerHandler ph = new TrackerHandler(connection);
                ph.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        try{
            Tracker p = new Tracker(args[0],Integer.parseInt(args[1]), args[2]);
            p.startServer();

        }catch (Exception e) {
            System.out.println("Usage: java Tracker ip port");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public class TrackerHandler extends Thread {
        Socket socket;

        public TrackerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() { //Protocol
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try {
                in = new ObjectInputStream(socket.getInputStream());
                PeerToTracker req = (PeerToTracker) in.readObject();
                System.out.printf("[Tracker %s , %d] GOT REQUEST " + req.toString(), getIp(), getPort());
                out = new ObjectOutputStream(socket.getOutputStream());

                if (req.method == Method.REGISTER) {
                    if (Registered_peers.containsKey(req.username)) {
                        FailureRegister(out);
                    } else {
                        Registered_peers.put(req.username, req.password);
                        SuccessRegister(out);
                    }
                } else if (req.method == Method.LOGIN) {
                    if (Registered_peers.containsKey(req.username) && Registered_peers.get(req.username).equals(req.password)) {
                        int token_id = getRandomTokenId();
                        SuccessLogin(out, token_id);
                        //in = new ObjectInputStream(socket.getInputStream());  //TODO CHECK IF IT IS WORKING
                        PeerToTracker secondinput = (PeerToTracker) in.readObject();
                        System.out.printf("[Tracker %s , %d] GOT SHARED_DIRECTORY " + req.toString(), getIp(), getPort());
                        Info peerinfo = new Info(secondinput.ip, secondinput.port, secondinput.username, secondinput.shared_directory);
                        TokenId_toInfo.put(token_id, peerinfo);
                        //TODO FILL Files_toToken array .
                    } else {
                        FailureLogin(out);
                    }
                } else if (req.method == Method.LOGOUT) {

                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void FillFiles_toToken(){
        for(String i : All_files){
            Files_toToken.put(i, new ArrayList<Integer>());
        }
    }

    public int getRandomTokenId() {
        int min=0;
        int max=100;
        int tokenid = 0;
        while(All_tokenids.contains(tokenid)) {
            tokenid = (int) ((Math.random() * (max - min)) + min);
        }
        return tokenid;
    }

    public void FailureRegister(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_REGISTER;
        out.writeObject(reply);
    }

    public void SuccessRegister(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_REGISTER;
        out.writeObject(reply);
    }

    public void FailureLogin(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGIN;
        out.writeObject(reply);
    }

    public void SuccessLogin(ObjectOutputStream out, int token) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        All_tokenids.add(token);
        reply.token_id = token;
        reply.statusCode = StatusCode.SUCCESSFUL_LOGIN;
        out.writeObject(reply);
    }
}
