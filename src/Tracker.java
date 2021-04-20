import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {
    ConcurrentHashMap<String, String> Registered_peers = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Info> Username_toInfo = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ConcurrentHashMap<String, Info>> Files_toInfo  = new ConcurrentHashMap<>();
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
        All_files = Util.readFileDownloadList(fileDownloadListPath);

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
                System.out.printf("[Tracker %s , %d] GOT REQUEST " + req.toString() + "\n", getIp(), getPort());

                if (req.method == Method.REGISTER) {
                    if (Registered_peers.containsKey(req.username)) {
                        FailureRegister(out);
                    } else {
                        Registered_peers.put(req.username, req.password);
                        Info peerInfo = new Info(req.username);
                        Username_toInfo.put(req.username, peerInfo);
                        SuccessRegister(out);
                    }
                } else if (req.method == Method.LOGIN) {
                    if (Registered_peers.containsKey(req.username) && Registered_peers.get(req.username).equals(req.password)) {
                        int token_id = getRandomTokenId();
                        All_tokenIds.add(token_id);
                        SuccessLogin(out, token_id);
                        PeerToTracker secondInput = (PeerToTracker) in.readObject();
                        System.out.printf("[Tracker %s , %d] GOT PEER INFO " + secondInput.toString() + " \n", getIp(), getPort());

                        Info infoTemp = Username_toInfo.get(req.username);
                        infoTemp.ip = secondInput.ip;
                        infoTemp.port = secondInput.port;
                        infoTemp.sharedDirectory = secondInput.sharedDirectory;

                        for(String i: infoTemp.sharedDirectory){
                            System.out.println(i);
                            Files_toInfo.get(i).put(secondInput.username, infoTemp);
                        }
                    } else {
                        FailureLogin(out);
                    }
                } else if (req.method == Method.LOGOUT) {
                    if(All_tokenIds.contains(req.token_id)) {
                        System.out.println("Token ID"+req.token_id);
                        All_tokenIds.remove((Integer) req.token_id);
                        ArrayList<String> filesOfRemoved = Username_toInfo.get(req.username).sharedDirectory;

                        for(String i: filesOfRemoved){
                            // It removes from Files_toInfo all the Shared_directory files in the Concurrent hashmap with key "req.token_id" which is the token given from the peer.
                            Files_toInfo.get(i).remove(req.username);
                        }
                        SuccessLogout(out);
                    }else{
                        FailureLogout(out);
                    }

                } else if (req.method == Method.LIST) {
                    replyList(out);
                } else if (req.method == Method.DETAILS) {
                    ConcurrentHashMap<String, Info> peersWithFile =  Files_toInfo.get(req.fileName);
                    ArrayList<Info> activeFiles = new ArrayList<>();
                    for(Map.Entry<String, Info> i : peersWithFile.entrySet()) {
                        StatusCode status = checkActive(i.getValue().ip, i.getValue().port);
                        if (status != null) {
                            if (!status.equals(StatusCode.PEER_ISACTIVE)) {
                                All_tokenIds.remove(i.getValue().tokenId);
                                ArrayList<String> filesOfRemoved = Username_toInfo.get(i.getKey()).sharedDirectory;

                                for (String j : filesOfRemoved) {
                                    Files_toInfo.get(j).remove(i.getKey());
                                }
                                peersWithFile.remove(i.getKey()); //TODO maybe wrong
                            } else {
                                activeFiles.add(i.getValue());
                            }
                        }
                    }
                    if(!peersWithFile.isEmpty()) {
                        System.out.println("Peers with the file:"+peersWithFile.size());
                        replyDetails(out, activeFiles);
                    }else{
                        System.out.println("No peer with this file");
                        replyDetailsNot(out);
                    }
                } else if (req.method == Method.NOTIFY_SUCCESSFUL) {
                    // Update Files_toInfo data structure, essentially we add peer's info to the array of the peers that have the specific file
                    Info infoTemp = Username_toInfo.get(req.username);
                    Files_toInfo.get(req.fileName).put(req.username, infoTemp);
                    // Moreover, increase count downloads index.
                    Username_toInfo.get(req.peerUsername).countDownloads++;
                } else if(req.method == Method.NOTIFY_FAILED){
                    Username_toInfo.get(req.peerUsername).countFailures++;
                }else{
                    System.out.println("Got unexpected request");
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
            System.out.println("[PEER %d] Connected to peer on port "+peerIp+" port "+peerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            AnyToPeer anytopeer = new AnyToPeer();
            anytopeer.method = Method.CHECK_ACTIVE_TRACKER_TO_PEER;
            System.out.println(anytopeer.toString());
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
            Files_toInfo.put(i, new ConcurrentHashMap<String, Info>());
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
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void SuccessRegister(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_REGISTER;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void FailureLogin(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGIN;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void SuccessLogin(ObjectOutputStream out, int token) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        All_tokenIds.add(token);
        reply.tokenΙd = token;
        reply.statusCode = StatusCode.SUCCESSFUL_LOGIN;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void SuccessLogout(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_LOGOUT;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void FailureLogout(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGOUT;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void replyList(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.allFiles = All_files;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void replyDetails(ObjectOutputStream out, ArrayList<Info> withFile) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.FILE_FOUND;
        reply.peerInfo = withFile;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void replyDetailsNot(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.FILE_NOTFOUND;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

}