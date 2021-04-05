import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class Peer {
    public int token_id;

    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String username;
    public String password;


    public void register(){
        Socket socket = null;
        ObjectOutputStream out = null;
        try {
            socket = new Socket(ip, port);
            System.out.printf("[PEER %d] Connected to broker on port %d , ip %s%n" ,getPort() , port , ip);

            out = new ObjectOutputStream(socket.getOutputStream());

            PeerToTracker peerToTracker = new PeerToTracker();
            peerToTracker.method = Method.REGISTER;

            out.writeObject(peerToTracker);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void login(){

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


                 //req = () in.readObject();

                System.out.printf("[PUBLISHER %s , %d] GOT REQUEST " + req.toString() , getIp() , getPort());
                out = new ObjectOutputStream(socket.getOutputStream());

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


            } catch (IOException e) {
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

    //constructor
    public Peer(String registerOrLogin,String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        if(registerOrLogin.equals('R')){
            register();
        }else if(registerOrLogin.equals('L')){
            login();
        }else{
            System.out.println("First Parameter must be set to R or L");
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

    public static void main(String[] args){
        try{
            // 0: REGISTER/LOGIN
            // 1: ip
            // 2 port
            Peer p = new Peer(args[0], args[1],Integer.parseInt(args[2]), args[3], args[4]);
            p.startServer();

        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
