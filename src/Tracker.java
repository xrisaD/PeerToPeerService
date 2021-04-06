import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers;
    ConcurrentHashMap<Integer, Info> TokenId_toInfo;
    ConcurrentHashMap<String, ArrayList<Integer>> Files_toToken;
    ArrayList<Integer> All_tokenids;
    ArrayList<String> All_files;
    private final String ip;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    private final int port;

    public Tracker(String ip, int port) {
        this.ip = ip;
        this.port = port;
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
                ΤrackerHandler ph = new ΤrackerHandler(connection);
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
            Tracker p = new Tracker(args[0],Integer.parseInt(args[1]));
            p.startServer();

        }catch (Exception e) {
            System.out.println("Usage: java Publisher ip port");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public class ΤrackerHandler extends Thread {
        Socket socket;

        public ΤrackerHandler(Socket socket) {
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
                        FailureRegister(req, out);
                    } else {
                        Registered_peers.put(req.username, req.password);
                        SuccessRegister(req, out);
                    }
                } else if (req.method == Method.LOGIN) {
                    if (Registered_peers.containsKey(req.username) && Registered_peers.get(req.username).equals(req.password)) {
                        int token_id = getRandomTokenId();
                        SuccessLogin(req, out, token_id);
                        //in = new ObjectInputStream(socket.getInputStream());  //TODO CHECK IF IT IS WORKING
                        PeerToTracker secondinput = (PeerToTracker) in.readObject();
                        System.out.printf("[Tracker %s , %d] GOT SHARED_DIRECTORY " + req.toString(), getIp(), getPort());
                        Info peerinfo = new Info(secondinput.ip, secondinput.port, secondinput.username, secondinput.shared_directory);
                        TokenId_toInfo.put(token_id, peerinfo);
                        //TODO FILL Files_toToken array .
                    } else {
                        FailureLogin(req, out);
                    }
                } else if (req.method == Method.LOGOUT) {

                }
                //if(Registered_peers.contains())

//                Request.RequestToPublisher req= (Request.RequestToPublisher) in.readObject();
//                System.out.printf("[PUBLISHER %s , %d] GOT REQUEST " + req.toString() , getIp() , getPort());
//                out = new ObjectOutputStream(socket.getOutputStream());
//                if(req.method == Request.Methods.PUSH) {
//                    if(req.artistName==null || req.songName==null){
//                        notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
//                    }else {
//                        push(req.artistName, req.songName.toLowerCase(), out);
//                    }
//                }else if(req.method == Request.Methods.SEARCH){
//                    if(req.artistName==null){
//                        notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
//                    }else{
//                        search(req.artistName, out);
//                    }
//                }else{
//                    notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
//                }

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

    public int getRandomTokenId() {
        int min=0;
        int max=100;
        int tokenid = 0;
        while(All_tokenids.contains(tokenid)) {
            tokenid = (int) ((Math.random() * (max - min)) + min);
        }
        return tokenid;
    }

    public void FailureRegister(PeerToTracker req, ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_REGISTER;
        out.writeObject(reply);
    }

    public void SuccessRegister(PeerToTracker req, ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_REGISTER;
        out.writeObject(reply);
    }

    public void FailureLogin(PeerToTracker req, ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGIN;
        out.writeObject(reply);
    }

    public void SuccessLogin(PeerToTracker req, ObjectOutputStream out, int token) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        All_tokenids.add(token);
        reply.token_id = token;
        reply.statusCode = StatusCode.SUCCESSFUL_LOGIN;
        out.writeObject(reply);
    }
}
