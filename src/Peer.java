import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

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

    public void askForNewUserName(){
        System.out.println("Please, choose a different username..");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        setUsername(username);
    }
    public void askForNewUserNameAndPassword(){
        System.out.println("Please, choose a different username and password..");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        String password = scanner.nextLine();
        setUsername(username);
        setPassword(password);

    }

    public StatusCode login(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("[PEER %d] Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send login request to tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.LOGIN;
            peerToTracker.username = username;
            peerToTracker.password = password;

            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println(reply.toString());

            if (reply.statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                setToken_id(reply.token_id);
                inform(out);
                return reply.statusCode;
            } else if (reply.statusCode == StatusCode.UNSUCCESSFUL_LOGIN) {
                return reply.statusCode;
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


    public StatusCode register(){
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            socket = new Socket(trackerIp, trackerPort);
            System.out.println("[PEER %d] Connected to Tracker on port "+trackerIp+" port "+trackerPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // send request to Tracker
            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.REGISTER;
            peerToTracker.username = username;
            peerToTracker.password = password;
            out.writeObject(peerToTracker);

            AnyToPeer reply = (AnyToPeer) in.readObject();
            System.out.println(reply.toString());
            return reply.statusCode;

        } catch (UnknownHostException e) {
            e.printStackTrace();
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
    public Peer(String ip, int port, String username, String password, String sharedDirectoryPath, String fileDownloadListPath) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sharedDirectoryPath = sharedDirectoryPath;

        this.fileTitles = Util.readFileDownloadList(fileDownloadListPath);
    }

    public static void main(String[] args){
        try{
            // Command Line Inputs:
            // 0: ip, 1: port
            // 2: username, 3: password
            // 4: shared_directory path 5: fileDownloadList.txt. path
            System.out.println("Start...");
            Peer p = new Peer(args[0], parseInt(args[1]), args[2], args[3], args[4], args[5]);

            System.out.println("Start...");
            PeerMainThread peerMainThread = new PeerMainThread(p);
            peerMainThread.start();


            System.out.println("Start...");
            p.startServer();

        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static class PeerMainThread extends Thread {
        Peer p;
        PeerMainThread(Peer p){
            this.p = p;
        }
        @Override
        public void run() {
            boolean registered = false;
            boolean loggedin = false;
            System.out.println("Start...");
            while(true){
                System.out.println("Choose: \n0:regster \n1:login");
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                int func = parseInt(input);

                // REGISTER
                if(func==0 && registered==false) {
                    StatusCode statusCode = p.register();
                    if (statusCode != null) {
                        if (statusCode == StatusCode.SUCCESSFUL_REGISTER) {
                            registered = true;
                        } else
                            p.askForNewUserName();
                    }
                }else if(func==0 && registered==true){
                    System.out.println("You are already registered");
                }
                // LOGIN
                else if(func==1 && loggedin==false){
                    StatusCode statusCode = p.login();
                    if(statusCode!=null) {
                        if (statusCode == StatusCode.SUCCESSFUL_LOGIN) {
                            loggedin = true;
                        } else {
                            p.askForNewUserNameAndPassword();
                        }
                    }
                }else if(func==1 && loggedin==true){
                    System.out.println("You are already logged in");
                }
                // DOWNLOAD
                else if(loggedin){
                    //katevasma file
                }

            }
        }
    }
}
