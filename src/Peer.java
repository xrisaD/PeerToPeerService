import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Peer {
    public int token_id;

    private String ip;
    private int port;

    public String username;
    public String password;

    public ArrayList<String> fileTitles;

    public String sharedDirectoryPath;

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

    public void register(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(ip, port);
            System.out.printf("[PEER %d] Connected to broker on port %d , ip %s%n" ,getPort() , port , ip);

            out = new ObjectOutputStream(socket.getOutputStream());

            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.REGISTER;
            peerToTracker.username = username;
            peerToTracker.password = password;

            out.writeObject(peerToTracker);

            in = new ObjectInputStream(socket.getInputStream());

            AnyToPeer reply = (AnyToPeer) in.readObject();
            if(reply.statusCode == StatusCode.UNSUCCESSFUL_REGISTER){
                //TODO: zhtaei neo user name
                register();
            } else if(reply.statusCode == StatusCode.SUCCESSFUL_REGISTER){
                //TODO: Rotaei thes na kaneis login? an pei nai login
                Boolean login = true;
                if(login) {
                    login();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void login(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(ip, port);
            System.out.printf("[PEER %d] Connected to broker on port %d , ip %s%n" ,getPort() , port , ip);

            out = new ObjectOutputStream(socket.getOutputStream());

            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.LOGIN;
            peerToTracker.username = username;
            peerToTracker.password = password;

            out.writeObject(peerToTracker);

            in = new ObjectInputStream(socket.getInputStream());
            AnyToPeer reply = (AnyToPeer) in.readObject();
            if (reply.statusCode == StatusCode.SUCCESSFUL_LOGIN){
                setToken_id(reply.token_id);
                inform(out);
            } else if (reply.statusCode == StatusCode.UNSUCCESSFUL_LOGIN){
                //TODO: ksanavale username + password

            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void inform(ObjectOutputStream out) throws IOException {
        PeerToTracker peerToTracker = new PeerToTracker();
        peerToTracker.shared_directory = this.fileTitles;
        peerToTracker.ip = this.ip;
        peerToTracker.port = this.port;
        peerToTracker.username = this.username;
        out.writeObject(peerToTracker);
    }
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
                in = new ObjectInputStream(socket.getInputStream());
                AnyToPeer req = (AnyToPeer) in.readObject();

                System.out.printf("[PUBLISHER %s , %d] GOT REQUEST " + req.toString() , getIp() , getPort());
                out = new ObjectOutputStream(socket.getOutputStream());

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

    /**
     * Server starts for Peers
     */
    public void startServer() {
        ServerSocket providerSocket = null;
        Socket connection = null;
        try {
            providerSocket = new ServerSocket(this.port, 10);

            System.out.println("Peer listening on port " + getPort());

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
    public Peer(String registerOrLogin,String ip, int port, String username, String password, String sharedDirectoryPath, String fineDownloadListPath) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sharedDirectoryPath = sharedDirectoryPath;

        // read file with files list


        if(registerOrLogin.equals('R')){
            register();
        }else if(registerOrLogin.equals('L')){
            login();
        }else{
            System.out.println("First Parameter must be set to R or L");
        }
    }

    public static void main(String[] args){
        try{
            // Command Line Inputs:
            // 0: REGISTER/LOGIN, R or P respectively
            // 1: ip, 2: port
            // 3: username, 4: password
            // 5: shared_directory path 6: fileDownloadList.txt. path
            Peer p = new Peer(args[0], args[1],Integer.parseInt(args[2]), args[3], args[4], args[5], args[6]);
            p.startServer();

        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
