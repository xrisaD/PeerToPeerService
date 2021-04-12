import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Info> TokenId_toInfo = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Info>> Files_toInfo = new ConcurrentHashMap<>();
    ArrayList<Integer> All_tokenIds = new ArrayList<>();
    ArrayList<String> All_files;
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
        All_files = Util.readfiledownloadlist(fileDownloadListPath);

        FillFiles_toToken();
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
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                PeerToTracker req = (PeerToTracker) in.readObject();
                System.out.printf("[Tracker %s , %d] GOT REQUEST " + req.toString(), getIp(), getPort());

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
//                        in = new ObjectInputStream(socket.getInputStream());  //TODO CHECK IF IT IS WORKING
                        PeerToTracker secondInput = (PeerToTracker) in.readObject();
                        System.out.printf("[Tracker %s , %d] GOT PEER INFO " + req.toString(), getIp(), getPort());
                        Info peerInfo = new Info(secondInput.ip, secondInput.port, secondInput.username, secondInput.shared_directory);
                        TokenId_toInfo.put(token_id, peerInfo);
                        // Fills Files_toToken array.
                        for(String i: peerInfo.Shared_directory){
                            Files_toInfo.get(i).put(token_id, peerInfo);
                        }
                    } else {
                        FailureLogin(out);
                    }
                } else if (req.method == Method.LOGOUT) {
                    if(All_tokenIds.contains(req.token_id)) {
                        All_tokenIds.remove(req.token_id);
                        ArrayList<String> filesOfRemoved = TokenId_toInfo.get(req.token_id).Shared_directory;
                        TokenId_toInfo.remove(req.token_id);
                        for(String i: filesOfRemoved){
                            // It removes from Files_toInfo all the Shared_directory files in the Concurrent hashmap with key "req.token_id" which is the token given from the peer.
                            Files_toInfo.get(i).remove(req.token_id);
                        }
                        SuccessLogout(out);
                    }else{
                        FailureLogout(out);
                    }

                }   else if (req.method == Method.LIST) {
                    replyList(out);
                }   else if (req.method == Method.DETAILS) {
                    ConcurrentHashMap<Integer, Info> peersWithFile =  Files_toInfo.get(req.fileName);
                    ArrayList<Info> activeFiles = new ArrayList<>();
                    for(Map.Entry<Integer, Info> i : peersWithFile.entrySet()){
                        StatusCode status = checkActive(i.getValue().ip, i.getValue().port);
                        if(!status.equals(StatusCode.PEER_ISACTIVE)){
                            All_tokenIds.remove(i.getKey());
                            ArrayList<String> filesOfRemoved = TokenId_toInfo.get(i.getKey()).Shared_directory;
                            TokenId_toInfo.remove(i.getKey());
                            for(String j: filesOfRemoved){
                                // It removes from Files_toInfo all the Shared_directory files in the Concurrent hashmap with key "req.token_id" which is the token given from the peer.
                                Files_toInfo.get(j).remove(i.getKey());
                            }
                            peersWithFile.remove(i);
                        }else{
                            activeFiles.add(i.getValue());
                        }
                    }
                    if(!peersWithFile.isEmpty()) {
                        replyDetails(out, activeFiles);
                    }else{
                        replyDetailsNot(out);
                    }
                }   else if (req.method == Method.NOTIFY_SUCCESSFUL) {

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

    public static StatusCode checkActive(String peerIp, int peerPort){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(peerIp, peerPort);
            System.out.println("[PEER %d] Any connected to peer on port "+peerIp+" port "+peerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send login request to tracker
            AnyToPeer anytopeer = new AnyToPeer();
            anytopeer.method = Method.CHECK_ACTIVE;

            out.writeObject(anytopeer);

            PeerToTracker reply = (PeerToTracker) in.readObject();
            System.out.println(reply.toString());

            return reply.statusCode;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try{
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void FillFiles_toToken(){
        for(String i : All_files){
            Files_toInfo.put(i, new ConcurrentHashMap<Integer, Info>());
        }
    }

    public int getRandomTokenId() {
        int min=0;
        int max=100;
        int tokenid = 0;
        while(All_tokenIds.contains(tokenid)) {
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
        All_tokenIds.add(token);
        reply.token_id = token;
        reply.statusCode = StatusCode.SUCCESSFUL_LOGIN;
        out.writeObject(reply);
    }

    public void SuccessLogout(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_LOGOUT;
        out.writeObject(reply);
    }

    public void FailureLogout(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGOUT;
        out.writeObject(reply);
    }

    public void replyList(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.All_files = All_files;
        out.writeObject(reply);
    }

    public void replyDetails(ObjectOutputStream out, ArrayList<Info> withFile) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.Peer_Info = withFile;
        out.writeObject(reply);
    }

    public void replyDetailsNot(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.FILE_NOTFOUND;
        out.writeObject(reply);
    }

}