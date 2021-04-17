import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import static java.lang.Integer.parseInt;

public class Peer {
    static final String trackerIp = "127.0.0.1";
    static final int trackerPort = 5000;

    public int token_id;
    private String ip;
    private int port;

    public String username;
    public String password;

    public ArrayList<String> fileTitles;

    public String sharedDirectoryPath;


    // Setters and getters
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setToken_id(int token_id) {
        this.token_id = token_id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // check if peer is active
    public StatusCode isActive(){
        if(token_id != -1) {
            return StatusCode.PEER_ISACTIVE;
        }else{
            return StatusCode.PEER_ISNOTACTIVE;
        }
    }

    // input methods

    // print message asking for new username and read one
    public void askForNewUserName(){
        System.out.println("Please, choose a different username..");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        setUsername(username);
    }

    // print message asking for new username and password and read them
    public void askForNewUserNameAndPassword(){
        System.out.println("Please, choose a different username and password..");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        String password = scanner.nextLine();
        setUsername(username);
        setPassword(password);

    }

    // request methods

    // check if a peer is active and return the answer
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

            AnyToPeer reply = (AnyToPeer) in.readObject();
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

    // get details for peers that have a specific file
    public  ArrayList<Info> details(String fileName){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.DETAILS;
            peerToTracker.fileName = fileName;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());

            if (reply.statusCode == StatusCode.FILE_FOUND){
                ArrayList<Info> peerInfo = reply.Peer_Info;
                return peerInfo;
            }else if(reply.statusCode == StatusCode.FILE_NOTFOUND){
                return null; // null means file not found
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
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

    // ask tracker for all available files
    public ArrayList<String> list(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.LIST;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());
            return reply.All_files;

        } catch (UnknownHostException e) {
            e.printStackTrace();
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

    // login to P2P system
    public StatusCode login(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send login request to tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.LOGIN;
            peerToTracker.username = this.username;
            peerToTracker.password = this.password;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());

            if (reply.statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                setToken_id(reply.token_id);
                inform(out);
                return reply.statusCode;
            } else if (reply.statusCode == StatusCode.UNSUCCESSFUL_LOGIN) {
                return reply.statusCode;
            }

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

    // logout from P2P system
    public StatusCode logout(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.LOGOUT;
            peerToTracker.token_id = token_id;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());
            return reply.statusCode;

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
        return null;
    }

    // register to P2P system
    public StatusCode register(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.REGISTER;
            peerToTracker.username = username;
            peerToTracker.password = password;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());
            return reply.statusCode;

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
        return null;
    }

    public void successfulNotify(String fileName, String username){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.NOTIFY_SUCCESSFUL;
            peerToTracker.fileName = fileName;
            peerToTracker.peerUsername = username;
            peerToTracker.username = username;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

        } catch (IOException e) {
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

    public void unsuccessfulNotify(String username){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.NOTIFY_FAILED;
            peerToTracker.peerUsername = username;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            System.out.println(peerToTracker.toString());

        } catch (IOException e) {
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

    public byte[] download(String fileName, String ipIp, int ipPort){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(ipIp, ipPort);
            System.out.println("PEER Connected to PEER on port "+ipIp+" port "+ipPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Peer
            AnyToPeer anyToPeer = new AnyToPeer();

            anyToPeer.method = Method.SIMPLE_DOWNLOAD;
            anyToPeer.fileName = fileName;

            out.writeObject(anyToPeer);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());
            if(reply.statusCode==StatusCode.UNSUCCESSFUL_LOGIN){
                return null;
            }else if(reply.statusCode==StatusCode.SUCCESSFUL_LOGIN){
                return reply.buffer;
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
        return null;
    }

    // infrom
    public void inform(ObjectOutputStream out) throws IOException {
        PeerToTracker peerToTracker = new PeerToTracker();
        for (String i: this.fileTitles){
            System.out.println(fileTitles);
        }
        peerToTracker.shared_directory = this.fileTitles;
        peerToTracker.ip = this.ip;
        peerToTracker.port = this.port;
        peerToTracker.username = this.username;
        System.out.println(peerToTracker.toString());
        out.writeObject(peerToTracker);
    }

    // computing a score for each peer
    public HashMap<Double, Info> computeScores(ArrayList<Info> peers){
        HashMap<Double, Info> scores = new HashMap<Double, Info>();
        for (int i=0; i < peers.size(); i++){
            long start = System. currentTimeMillis();
            Info peer = peers.get(i);
            String ip = peer.ip;
            int port = peer.port;
            checkActive(ip, port);
            long end = System.currentTimeMillis();
            long elapsedTime = end - start;

            double score = elapsedTime*((9/10)^(peer.count_downloads))*((12/10)^(peer.count_failures));
            scores.put(score, peer);
        }
        return scores;
    }

    // try to download the file selecting each time the best peer
    public boolean simpleDownload(String fileName,  HashMap<Double, Info> scores) {
        byte[] file = null;
        double max = 0;
        while(scores.size()>0){
            max = Collections.max(scores.keySet()); // best peer
            file = download(fileName, scores.get(max).ip, scores.get(max).port);
            if(file==null){
                //notify
                unsuccessfulNotify(scores.get(max).username);
            }else{
                // save file to shared_dir
                Util.saveFile(sharedDirectoryPath, fileName, file);
                //notify Tracker
                successfulNotify(fileName, scores.get(max).username);
                return true;
            }
            scores.remove(max);
        }
        return false;
    }
    // thread which handle a request from Peer or Tracker
    public class PeerHandler extends Thread{
        Socket socket;
        public PeerHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){ //Protocol
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try{
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                AnyToPeer req = (AnyToPeer) in.readObject();

                System.out.printf("PEER GOT REQUEST " + req.toString() );

                if(req.method == Method.CHECK_ACTIVE){
                    StatusCode statusCode = isActive();
                    PeerToTracker peerToTracker = new PeerToTracker();
                    peerToTracker.statusCode = statusCode;
                    System.out.println(peerToTracker.toString());
                    out.writeObject(peerToTracker);

                }else if(req.method == Method.SIMPLE_DOWNLOAD){
                    if(fileTitles.contains(req.fileName)){
                        AnyToPeer anyToPeer = new AnyToPeer();
                        anyToPeer.statusCode = StatusCode.FILE_FOUND;
                        anyToPeer.buffer = Util.loadFile(sharedDirectoryPath, req.fileName);
                        out.writeObject(anyToPeer);
                    }else{
                        AnyToPeer anyToPeer = new AnyToPeer();
                        anyToPeer.statusCode = StatusCode.FILE_NOTFOUND;
                        out.writeObject(anyToPeer);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if(socket != null) socket.close();
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    // Server starts for Peers
    public void startServer() {
        System.out.println("Start server...");

        ServerSocket providerSocket = null;
        Socket connection = null;
        try {
            providerSocket = new ServerSocket(this.port, 10);

            System.out.println("PEER listening on port " + getPort() + " and ip " +getIp());

            while (true) {
                connection = providerSocket.accept();
                //We start a thread
                //this thread will do the communication
                PeerHandler ph = new PeerHandler(connection);
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

    //constructor
    public Peer(String ip, int port, String username, String password, String sharedDirectoryPath) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sharedDirectoryPath = sharedDirectoryPath;

        this.fileTitles = Util.readSharedDirectory(sharedDirectoryPath);
        for (String file: this.fileTitles) {
            System.out.println(file);
        }
    }

    public static void main(String[] args){
        try{
            // Command Line Inputs:
            // 0: ip, 1: port
            // 2: username, 3: password
            // 4: shared_directory path 5: fileDownloadList.txt. path
            Peer p = new Peer(args[0], parseInt(args[1]), args[2], args[3], args[4]);

            PeerMainThread peerMainThread = new PeerMainThread(p);
            peerMainThread.start();

            p.startServer();

        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
