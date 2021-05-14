import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Integer.parseInt;

public class Peer {
    static final String trackerIp = "127.0.0.1";
    static final int trackerPort = 5000;

    Info myInfo;
    public int token_id;
    private String ip;
    private int port;

    public String username;
    public String password;

    public ArrayList<String> fileNames;

    public String sharedDirectoryPath;

    public int partitionSize = 500000; // 0.5 MB

    public AtomicBoolean lockServe = new AtomicBoolean(false);
    public final List<AnyToPeer> serveRequests = Collections.synchronizedList(new ArrayList<>());

    public AtomicBoolean lockColDown = new AtomicBoolean(false);
    public final List<AnyToPeer> colDownRequests = Collections.synchronizedList(new ArrayList<>());

    // Seeder's files
    ConcurrentHashMap<String, byte[][]> completedFiles = new ConcurrentHashMap<String, byte[][]>(); // filename -> array of partitions

    // Non Seeder's files
    ConcurrentHashMap<String , ArrayList<Partition>> nonCompletedFiles = new ConcurrentHashMap<>();

    ConcurrentHashMap<String,Integer> usernameToDownloadedFiles = new ConcurrentHashMap<String,Integer>();

    // this structure will be updated by the peer's main thread
    // when peer receives a list from the tracker with all the peers that have the file
    // we can get this number by getting the number of parts that a seeder has
    // because a seeder has all the parts of a file
    ConcurrentHashMap<String,Integer> fileToNumberOfPartitions = new ConcurrentHashMap<String,Integer>();

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

            // send active request to peer
            AnyToPeer anytopeer = new AnyToPeer();
            anytopeer.method = Method.CHECK_ACTIVE_PEER_TO_PEER;
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
                ArrayList<Info> peerInfo = reply.peerInfo;
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

    // get details for peers that have a specific file
    public  ConcurrentHashMap<String, Info> allPeers(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("PEER Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.ALL_PEERS;
            System.out.println(peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());

            ConcurrentHashMap<String, Info> usernameToInfo = reply.usernameToInfo;
            return usernameToInfo;

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
            System.out.println("REPLY: " + peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: " + reply.toString());
            return reply.allFiles;

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
            System.out.println("REPLY: " + peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: " + reply.toString());

            if (reply.statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                setToken_id(reply.tokenΙd);
                inform(out);
                myInfo = new Info(this.username, this.ip, this.port);
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
            peerToTracker.username = username;
            System.out.println("REPLY: " + peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: " + reply.toString());

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
            System.out.println("REPLY: " + peerToTracker.toString());
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: " + reply.toString());

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


    public void collaborativeDownloadOrSeeederServe(Method method, String fileName, Info info, Info myInfo, byte[] part, int id){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(info.ip, info.port);
            System.out.println("PEER Connected to Peer on port "+info.ip+" port "+info.port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Peer
            AnyToPeer anyToPeer = new AnyToPeer();
            anyToPeer.method = method;
            anyToPeer.fileName = fileName;
            anyToPeer.myInfo = myInfo;
            anyToPeer.buffer = part;
            anyToPeer.id = id;

            System.out.println(anyToPeer.toString());
            out.writeObject(anyToPeer);

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

    // notify trackr that a file downloaded successfully from a specific peer
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

    // notify trackr that a file downloaded unsuccessfully from a specific peer
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
            System.out.println("REPLY: " + peerToTracker.toString());
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

    // download a song from a specific peer
    public byte[] download(String fileName, String peerIp, int peerPort){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(peerIp, peerPort);
            System.out.println("PEER Connected to PEER on port "+ peerIp +" port "+ peerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Peer
            AnyToPeer anyToPeer = new AnyToPeer();
            anyToPeer.method = Method.SIMPLE_DOWNLOAD;
            anyToPeer.fileName = fileName;

            out.writeObject(anyToPeer);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println("REPLY: "+reply.toString());
            if(reply.statusCode==StatusCode.FILE_NOTFOUND){
                return null;
            }else if(reply.statusCode==StatusCode.FILE_FOUND){
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

    // inform tracker about the files the peer has in the shared directory
    public void inform(ObjectOutputStream out) throws IOException {
        // read peer's files
        this.fileNames = Util.readSharedDirectory(sharedDirectoryPath);
        for (String file: this.fileNames) {
            System.out.println(file);
        }
        HashMap<String, ArrayList<Integer>> pieces = new HashMap<String, ArrayList<Integer>>();
        HashMap<String, Boolean> seederBit = new HashMap<String, Boolean>();
        if(this.fileNames.size()>=1) {
            pieces = partition();
            seederBit = createSeederBits();
        }
        PeerToTracker peerToTracker = new PeerToTracker();
        peerToTracker.method = Method.INFORM;
        peerToTracker.sharedDirectory = this.fileNames;
        peerToTracker.ip = this.ip;
        peerToTracker.port = this.port;
        peerToTracker.username = this.username;

        peerToTracker.pieces = pieces;
        peerToTracker.seederBit = seederBit;

        System.out.println(peerToTracker.toString());
        out.writeObject(peerToTracker);
    }

    // set true to all bits
    public HashMap<String, Boolean> createSeederBits(){
        HashMap<String, Boolean> seederBits = new HashMap<String, Boolean>();
        for (int i = 0; i < this.fileNames.size(); i++){
            seederBits.put(this.fileNames.get(i), true);
        }
        return seederBits;
    }

    // TODO: TESTS
    // partition all files
    public HashMap<String, ArrayList<Integer>> partition(){
        HashMap<String, ArrayList<Integer>> pieces = new HashMap<String, ArrayList<Integer>>();
        for (int i = 0; i < this.fileNames.size(); i++){
            // load the file and break it into pieces
            byte[] file = Util.loadFile(sharedDirectoryPath, this.fileNames.get(i));
            byte[][] filePartition = Util.divide(file, this.partitionSize);
            completedFiles.put(this.fileNames.get(i), filePartition);
            ArrayList<Integer> parts = Util.getNumbersInRange(1, filePartition.length);
            pieces.put(this.fileNames.get(i), parts);
        }
        return pieces;
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

            double score = elapsedTime*((9/10)^(peer.countDownloads))*((12/10)^(peer.countFailures));
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
            //TODO: refactor this method
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try{
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                AnyToPeer req = (AnyToPeer) in.readObject();

                System.out.println("PEER GOT REQUEST " + req.toString() );

                if(req.method == Method.CHECK_ACTIVE_TRACKER_TO_PEER || req.method == Method.CHECK_ACTIVE_PEER_TO_PEER) {
                    // check if peer is active and answer
                    StatusCode statusCode = isActive();
                    replyActive(statusCode, out);
                }else if(req.method == Method.SIMPLE_DOWNLOAD){
                    // check if peer has the file
                    if(fileNames.contains(req.fileName)){
                        // send the file
                        AnyToPeer anyToPeer = new AnyToPeer();
                        anyToPeer.statusCode = StatusCode.FILE_FOUND;
                        anyToPeer.buffer = Util.loadFile(sharedDirectoryPath, req.fileName);
                        out.writeObject(anyToPeer);
                    }else{
                        // answer that peer doesn't have the file
                        // maybe the request was wrong
                        AnyToPeer anyToPeer = new AnyToPeer();
                        anyToPeer.statusCode = StatusCode.FILE_NOTFOUND;
                        out.writeObject(anyToPeer);
                    }
                }else if(req.method == Method.SEEDER_SERVE){
                    // this is a method that only seeders receive from other peers who are not seeders
                    // save all requests
                    synchronized (serveRequests){
                        serveRequests.add(req);
                    }
                    // If true, is the first request
                    if(lockServe.compareAndExchange(false, true)){
                        try{
                            Thread.sleep(200); // the first thread waits for 200msc, in case it receives more requests
                        } catch(InterruptedException e){
                            System.out.println(e);
                        }

                        // after 200msec only the first thread gets all the requests
                        // saves them and set the parameters and structure at the initial states
                        // so the process can start again
                        ArrayList<AnyToPeer> tempRequests;
                        synchronized (serveRequests) {
                            tempRequests = new ArrayList<>(colDownRequests);
                            serveRequests.clear();
                            lockServe.compareAndExchange(true, false);
                        }

                        // select a random request
                        int randomPeer = ThreadLocalRandom.current().nextInt(0, tempRequests.size());
                        AnyToPeer selectedReq = tempRequests.get(randomPeer);
                        if(completedFiles.containsKey(selectedReq.fileName)){
                            // get all file's parts
                            byte[][] sendRandom = completedFiles.get(selectedReq.fileName);
                            // select a random part
                            int randomByte = ThreadLocalRandom.current().nextInt(0, sendRandom.length);
                            // send the partition to the peer
                            collaborativeDownloadOrSeeederServe(Method.SEEDER_SERVE_SUCCESSFUL, selectedReq.fileName, selectedReq.myInfo, myInfo, sendRandom[randomByte], randomByte);
                            // remove the selected request and answer to all other requests that they didn't selected
                            tempRequests.remove(randomPeer);
                            answerAllWithANegativeResponse(tempRequests);
                        }
                    }
                }else if(req.method == Method.COLLABORATIVE_DOWNLOAD){
                    // this is a method that only non seeders receive from other peers who are not seeders

                    // save all requests
                    synchronized (colDownRequests){
                        colDownRequests.add(req);
                    }
                    if(lockColDown.compareAndExchange(false, true)){
                        try{
                            Thread.sleep(200);
                        } catch(InterruptedException e){
                            System.out.println(e); // the first thread waits for 200msc, in case it receives more requests
                        }

                        // after 200msec only the first thread gets all the requests
                        // saves them and set the parameters and structure at the initial states
                        // so the process can start again
                        ArrayList<AnyToPeer> tempRequests;
                        synchronized (colDownRequests) {
                            tempRequests = new ArrayList<>(colDownRequests);
                            colDownRequests.clear();
                            lockColDown.compareAndExchange(true, false);
                        }

                        // in case peer received only one request
                        if(tempRequests.size() == 1) {
                            AnyToPeer theRequest = tempRequests.get(0);

                            // peer may received some parts
                            if(theRequest.buffer != null){
                                // save parts
                                String fileName = theRequest.fileName;
                                int id = theRequest.id;
                                Partition partition = new Partition(theRequest.buffer, id);
                                nonCompletedFiles.get(fileName).add(partition);
                                // refresh the counter for this peer using its username
                                refreshCounter(theRequest.myInfo.username);
                            }

                            // answer to the peer
                            Info info = tempRequests.get(0).myInfo;
                            String fileName = tempRequests.get(0).fileName;
                            checkIfPeerHasAllPartAndDownload(info, fileName);

                        }else{
                            // save all partitions
                            saveAllPartitions(tempRequests);

                            int possibility = ThreadLocalRandom.current().nextInt(0, 100);

                            if(possibility < 20){ // p=0.2
                                int randomPeer = ThreadLocalRandom.current().nextInt(0, tempRequests.size());
                                Info info = tempRequests.get(randomPeer).myInfo;
                                String fileName = tempRequests.get(randomPeer).fileName;
                                checkIfPeerHasAllPartAndDownload(info, fileName);

                                tempRequests.remove(randomPeer); // remove the selected request
                            }else if(possibility < 60){ // p=0.4

                                ConcurrentHashMap<String, Info> allPeers = allPeers(); // ask tracker for information about all peers
                                HashMap<Info, String> tmpPeerToFile = new HashMap<Info, String>();
                                HashMap<Info, Integer> tmpPeerToRequest = new HashMap<Info, Integer>();

                                // get all peer's info
                                ArrayList<Info> peers = new ArrayList<Info>();
                                for (int i=0; i < tempRequests.size(); i++){
                                    // get only the information about the peers that sent us a request
                                    peers.add(allPeers.get(tempRequests.get(i)));

                                    // update tmp data structures
                                    tmpPeerToFile.put(tempRequests.get(i).myInfo, tempRequests.get(i).fileName);
                                    tmpPeerToRequest.put(tempRequests.get(i).myInfo, i);
                                }

                                if(peers!=null) {
                                    // find the best peer and download
                                    HashMap<Double, Info> scores = computeScores(peers);
                                    Double max = Collections.max(scores.keySet()); // best peer
                                    Info bestPeer = scores.get(max);

                                    checkIfPeerHasAllPartAndDownload(bestPeer, tmpPeerToFile.get(bestPeer));
                                    tempRequests.remove(tmpPeerToRequest.get(bestPeer)); // remove the selected request
                                }
                            }else{ // p=0.4

                            }
                            // answer to all non selected requests
                            answerAllWithANegativeResponse(tempRequests);
                        }
                    }
                }else if(req.method == Method.SEEDER_SERVE_SUCCESSFUL || req.method == Method.COLLABORATIVE_DOWNLOAD_NOT_ANSWER){
                    // just save the sent partition
                    String fileName = req.fileName;
                    int id = req.id;
                    Partition partition = new Partition(req.buffer, id);
                    nonCompletedFiles.get(fileName).add(partition);
                    refreshCounter(req.myInfo.username); // refresh counter for this specific user
                }else if(req.method == Method.NON_SELECTED){
                    // TODO: enhmerwsh domwn gia autoModeThread
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

    private void replyActive(StatusCode statusCode, ObjectOutputStream out) throws IOException {
        PeerToTracker peerToTracker = new PeerToTracker();
        peerToTracker.statusCode = statusCode;
        System.out.println("REPLY: " + peerToTracker.toString());
        out.writeObject(peerToTracker);
    }

    private void saveAllPartitions(ArrayList<AnyToPeer> tempRequests) {
        for (int i=0; i<tempRequests.size(); i++){
            if(tempRequests.get(i).buffer != null){
                // save partition
                String fileName = tempRequests.get(i).fileName;
                if(tempRequests.get(i).buffer!=null) {
                    if(!nonCompletedFiles.containsKey(fileName)) {
                        nonCompletedFiles.put(fileName, new ArrayList<Partition>());
                    }

                    int id = tempRequests.get(i).id;
                    Partition partition = new Partition(tempRequests.get(i).buffer, id);
                    nonCompletedFiles.get(fileName).add(partition);
                    refreshCounter(tempRequests.get(i).myInfo.username);
                }
            }
        }
    }

    private void refreshCounter(String username) {
        // refresh the counter for this username
        if(usernameToDownloadedFiles.containsKey(username)){
            usernameToDownloadedFiles.put(username, usernameToDownloadedFiles.get(username) + 1);
        }else{
            usernameToDownloadedFiles.put(username, 0);
        }
    }

    private void answerAllWithANegativeResponse(ArrayList<AnyToPeer> allRequests){
        for (int i = 0; i < allRequests.size(); i++){
            negativeAnswer(allRequests.get(i).myInfo);
        }
    }
    // login to P2P system
    public StatusCode negativeAnswer(Info info){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(info.ip, info.port);
            System.out.println("PEER Connected to Peer on port "+info.ip+" port "+info.port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send login request to tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.NON_SELECTED;
            System.out.println("REPLY: " + peerToTracker.toString());
            out.writeObject(peerToTracker);

        } catch (IOException e) {
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

    // check if peer has all the parts
    // we will do the same checking twice (once here and also in the main peer's thread) in order to avoid redundant messages
    private void checkIfPeerHasAllPartAndDownload(Info info, String fileName) {
        // if peer doesnt have all the parts
        if(fileToNumberOfPartitions.containsKey(fileName) && nonCompletedFiles.containsKey(fileName)
                && fileToNumberOfPartitions.get(fileName) > nonCompletedFiles.get(fileName).size()) {
            collaborativeDownload(info, fileName, Method.COLLABORATIVE_DOWNLOAD);
        }else{
            // ask for the peer not to send you back a partition, because you have downloaded all the parts
            // when peer has all parts (is a seeder) the file's info will be moved to allPartitions
            collaborativeDownload(info, fileName, Method.COLLABORATIVE_DOWNLOAD_NOT_ANSWER);
        }

    }

    public void collaborativeDownload(Info peer, String fileName, Method method){
        if(nonCompletedFiles.containsKey(fileName) && nonCompletedFiles.get(fileName).size()>0){ // false if the peer has become a seeder and the request was wrong
            ArrayList<Partition> partitions = nonCompletedFiles.get(fileName);
            int randomPart = ThreadLocalRandom.current().nextInt(0, partitions.size());
            collaborativeDownloadOrSeeederServe(method, fileName, peer, myInfo, partitions.get(randomPart).data, partitions.get(randomPart).id);
        }
    }


    // Server starts for Peer
    public void startServer() {

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

    }

    public static void main(String[] args){
        try{
            // Command Line Inputs:
            // 0: ip, 1: port
            // 2: username, 3: password
            // 4: shared_directory path
            Peer p = new Peer(args[0], parseInt(args[1]), args[2], args[3], args[4]);
            boolean autoMode = Boolean.parseBoolean(args[5]);

            if(!autoMode) {
                PeerMainThread peerMainThread = new PeerMainThread(p);
                // start peer's thread for command line requests
                peerMainThread.start();
            }else{
                // Project part 2 logic goes here
                PeerAutoModeThread peerAutoModeThread = new PeerAutoModeThread(p);
                peerAutoModeThread.start();
            }
            // start server that get requests from the tracker or other peers
            p.startServer();

        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
