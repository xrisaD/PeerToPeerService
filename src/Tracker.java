import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {
    ConcurrentHashMap<String, String> registeredPeers = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Info> usernameToInfo = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ConcurrentHashMap<String, Info>> filesToInfo = new ConcurrentHashMap<>();
    ArrayList<Integer> allTokenIds = new ArrayList<>();
    ArrayList<String> allFiles;
    private final String ip;
    private final int port;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Tracker(String ip, int port, String fileDownloadListPath) {
        this.ip = ip;
        this.port = port;
        // Reads all files from specific file in given path.
        allFiles = Util.readFileDownloadList(fileDownloadListPath);

        fillFilesToToken();
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
                    if (registeredPeers.containsKey(req.username)) {
                        failureRegister(out);
                    } else {
                        registeredPeers.put(req.username, req.password);
                        Info peerInfo = new Info(req.username);
                        usernameToInfo.put(req.username, peerInfo);
                        successRegister(out);
                    }
                } else if (req.method == Method.LOGIN) {
                    if (registeredPeers.containsKey(req.username) && registeredPeers.get(req.username).equals(req.password)) {
                        int token_id = getRandomTokenId();
                        allTokenIds.add(token_id);
                        successLogin(out, token_id);
                        PeerToTracker secondInput = (PeerToTracker) in.readObject();
                        System.out.printf("[Tracker %s , %d] GOT PEER INFO " + secondInput.toString() + " \n", getIp(), getPort());

                        Info infoTemp = usernameToInfo.get(req.username);
                        infoTemp.ip = secondInput.ip;
                        infoTemp.port = secondInput.port;
                        infoTemp.Shared_directory = secondInput.shared_directory;

                        for(String i: infoTemp.Shared_directory){
                            System.out.println(i);
                            filesToInfo.get(i).put(secondInput.username, infoTemp);
                        }
                    } else {
                        failureLogin(out);
                    }
                } else if (req.method == Method.LOGOUT) {
                    if(allTokenIds.contains(req.token_id)) {
                        System.out.println("Token ID"+req.token_id);
                        allTokenIds.remove((Integer) req.token_id);
                        ArrayList<String> filesOfRemoved = usernameToInfo.get(req.username).Shared_directory;

                        for(String i: filesOfRemoved){
                            // It removes from Files_toInfo all the Shared_directory files in the Concurrent hashmap with key "req.token_id" which is the token given from the peer.
                            filesToInfo.get(i).remove(req.username);
                        }
                        successLogout(out);
                    }else{
                        failureLogout(out);
                    }

                } else if (req.method == Method.LIST) {
                    replyList(out);
                } else if (req.method == Method.DETAILS) {
                    ConcurrentHashMap<String, Info> peersWithFile =  filesToInfo.get(req.fileName);
                    ArrayList<Info> activeFiles = new ArrayList<>();
                    for(Map.Entry<String, Info> i : peersWithFile.entrySet()) {
                        StatusCode status = checkActive(i.getValue().ip, i.getValue().port);
                        if (status != null) {
                            if (!status.equals(StatusCode.PEER_ISACTIVE)) {
                                allTokenIds.remove(i.getValue().tokenId);
                                ArrayList<String> filesOfRemoved = usernameToInfo.get(i.getKey()).Shared_directory;

                                for (String j : filesOfRemoved) {
                                    filesToInfo.get(j).remove(i.getKey());
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
                    Info infoTemp = usernameToInfo.get(req.username);
                    filesToInfo.get(req.fileName).put(req.username, infoTemp);
                    // Moreover, increase count downloads index.
                    usernameToInfo.get(req.peerUsername).count_downloads++;
                } else if(req.method == Method.NOTIFY_FAILED){
                    usernameToInfo.get(req.peerUsername).count_failures++;
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

    public void fillFilesToToken(){
        for(String i : allFiles){
            filesToInfo.put(i, new ConcurrentHashMap<String, Info>());
        }
    }

    public int getRandomTokenId() {
        int min=0;
        int max=100;
        int tokenid = 0;
        while(allTokenIds.contains(tokenid)) {
            tokenid = (int) ((Math.random() * (max - min)) + min);
        }
        return tokenid;
    }

    public void failureRegister(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_REGISTER;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void successRegister(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_REGISTER;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void failureLogin(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGIN;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void successLogin(ObjectOutputStream out, int token) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        allTokenIds.add(token);
        reply.token_id = token;
        reply.statusCode = StatusCode.SUCCESSFUL_LOGIN;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void successLogout(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.SUCCESSFUL_LOGOUT;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void failureLogout(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.UNSUCCESSFUL_LOGOUT;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void replyList(ObjectOutputStream out) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.All_files = allFiles;
        System.out.println(reply.toString());
        out.writeObject(reply);
    }

    public void replyDetails(ObjectOutputStream out, ArrayList<Info> withFile) throws IOException {
        AnyToPeer reply = new AnyToPeer();
        reply.statusCode = StatusCode.FILE_FOUND;
        reply.Peer_Info = withFile;
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